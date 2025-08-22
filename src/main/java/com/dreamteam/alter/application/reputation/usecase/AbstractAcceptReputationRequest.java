package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AcceptReputationRequestDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationKeywordMapDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.reputation.entity.Reputation;
import com.dreamteam.alter.domain.reputation.entity.ReputationKeyword;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationKeywordQueryRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRepository;

import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
public abstract class AbstractAcceptReputationRequest<A> {

    protected final ReputationRepository reputationRepository;
    protected final ReputationKeywordQueryRepository reputationKeywordQueryRepository;

    public final void execute(A actor, Long requestId, AcceptReputationRequestDto request) {
        // 1. 평판 요청 조회 및 검증
        ReputationRequest reputationRequest = findAndValidateReputationRequest(actor, requestId);

        // 2. 평판 키워드 검증 및 조회
        Set<ReputationKeywordMapDto> keywords = request.getKeywords();
        Map<String, ReputationKeyword> keywordMap = validateAndGetKeywords(keywords);

        // 3. 평판 생성 및 키워드 매핑
        createReputation(reputationRequest, actor, keywordMap, keywords);

        // 4. 평판 요청 상태를 완료로 변경
        reputationRequest.accept();
    }

    protected abstract ReputationRequest findAndValidateReputationRequest(A actor, Long requestId);

    protected final void createReputation(
        ReputationRequest reputationRequest,
        A actor,
        Map<String, ReputationKeyword> keywordMap,
        Set<ReputationKeywordMapDto> keywords
    ) {
        ReputationInfo reputationInfo = getReputationInfo(reputationRequest, actor);

        Reputation reputation = reputationRepository.save(
            Reputation.create(
                reputationRequest,
                reputationInfo.writerType(),
                reputationInfo.writerId(),
                reputationRequest.getRequesterType(),
                reputationRequest.getRequesterId(),
                reputationInfo.workspace()
            )
        );

        reputation.addReputationKeywordMap(keywords, keywordMap);
    }

    protected abstract ReputationInfo getReputationInfo(ReputationRequest reputationRequest, A actor);

    protected final Map<String, ReputationKeyword> validateAndGetKeywords(Set<ReputationKeywordMapDto> keywords) {
        if (keywords.size() < 2 || keywords.size() > 5) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "평판 키워드는 2개 이상 5개 이하로 선택해야 합니다.");
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

    protected record ReputationInfo(
        ReputationType writerType,
        Long writerId,
        Workspace workspace
    ) {}
}
