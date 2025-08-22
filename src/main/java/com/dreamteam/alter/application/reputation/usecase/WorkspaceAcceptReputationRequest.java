package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AcceptReputationRequestDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationKeywordMapDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.reputation.entity.Reputation;
import com.dreamteam.alter.domain.reputation.entity.ReputationKeyword;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.port.inbound.WorkspaceAcceptReputationRequestUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationKeywordQueryRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestQueryRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestStatus;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service("workspaceAcceptReputationRequest")
@RequiredArgsConstructor
@Transactional
public class WorkspaceAcceptReputationRequest implements WorkspaceAcceptReputationRequestUseCase {

    private final ReputationRequestQueryRepository reputationRequestQueryRepository;
    private final ReputationRequestRepository reputationRequestRepository;
    private final ReputationRepository reputationRepository;
    private final ReputationKeywordQueryRepository reputationKeywordQueryRepository;
    private final WorkspaceQueryRepository workspaceQueryRepository;

    @Override
    public void execute(ManagerActor actor, Long requestId, AcceptReputationRequestDto request) {
        // 1. 평판 요청 조회 및 검증
        ReputationRequest reputationRequest = reputationRequestQueryRepository.findById(requestId);

        if (ObjectUtils.isEmpty(reputationRequest)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "평판 요청을 찾을 수 없습니다.");
        }

        if (!ReputationType.WORKSPACE.equals(reputationRequest.getTargetType())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "평판 요청의 대상이 업장이 아닙니다.");
        }

        if (!ReputationRequestStatus.REQUESTED.equals(reputationRequest.getStatus())) {
            throw new CustomException(ErrorCode.UNMODIFIABLE_STATUS, "이미 처리된 평판 요청입니다.");
        }

        // 2. 업장 권한 검증
        Workspace workspace = workspaceQueryRepository.findById(reputationRequest.getTargetId())
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        if (!workspace.getManagerUser().equals(actor.getManagerUser())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 업장에 대한 관리 권한이 없습니다.");
        }

        // 3. 평판 키워드 검증 및 조회
        Set<ReputationKeywordMapDto> keywords = request.getKeywords();
        Map<String, ReputationKeyword> keywordMap = validateAndGetKeywords(keywords);

        // 4. 평판 생성
        Reputation reputation = reputationRepository.save(
            Reputation.create(
                reputationRequest,
                ReputationType.WORKSPACE,
                workspace.getId(),
                reputationRequest.getRequesterType(),
                reputationRequest.getRequesterId(),
                reputationRequest.getWorkspace()
            )
        );

        // 5. 평판 키워드 매핑 추가
        reputation.addReputationKeywordMap(keywords, keywordMap);

        // 6. 평판 요청 상태를 완료로 변경
        reputationRequest.accept();
        reputationRequestRepository.save(reputationRequest);
    }

    private Map<String, ReputationKeyword> validateAndGetKeywords(Set<ReputationKeywordMapDto> keywords) {
        if (keywords.size() < 2 || keywords.size() > 6) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "평판 키워드는 2개 이상 6개 미만으로 선택해야 합니다.");
        }

        Map<String, ReputationKeyword> keywordMap =
            reputationKeywordQueryRepository.findByIdList(ReputationKeywordMapDto.extractKeywordIds(keywords))
                .stream()
                .collect(Collectors.toMap(ReputationKeyword::getId, keyword -> keyword));

        if (keywordMap.size() != keywords.size()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "일부 평판 키워드가 존재하지 않습니다. 다시 확인해주세요.");
        }

        return keywordMap;
    }
}
