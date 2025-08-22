package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AcceptReputationRequestDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationKeywordMapDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.reputation.entity.Reputation;
import com.dreamteam.alter.domain.reputation.entity.ReputationKeyword;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.port.inbound.AppAcceptReputationRequestUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationKeywordQueryRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestQueryRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.user.context.AppActor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service("appAcceptReputationRequest")
@RequiredArgsConstructor
@Transactional
public class AppAcceptReputationRequest implements AppAcceptReputationRequestUseCase {

    private final ReputationRequestQueryRepository reputationRequestQueryRepository;
    private final ReputationRequestRepository reputationRequestRepository;
    private final ReputationRepository reputationRepository;
    private final ReputationKeywordQueryRepository reputationKeywordQueryRepository;

    @Override
    public void execute(AppActor actor, Long requestId, AcceptReputationRequestDto request) {
        // 1. 평판 요청 조회 및 검증
        ReputationRequest reputationRequest = reputationRequestQueryRepository.findByTargetAndId(
            ReputationType.USER, 
            actor.getUserId(), 
            requestId
        );

        if (ObjectUtils.isEmpty(reputationRequest)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "평판 요청을 찾을 수 없습니다.");
        }

        // 2. 평판 키워드 검증 및 조회
        Set<ReputationKeywordMapDto> keywords = request.getKeywords();
        Map<String, ReputationKeyword> keywordMap = validateAndGetKeywords(keywords);

        // 3. 평판 생성
        Reputation reputation = reputationRepository.save(
            Reputation.create(
                reputationRequest,
                ReputationType.USER,
                actor.getUserId(),
                reputationRequest.getRequesterType(),
                reputationRequest.getRequesterId(),
                reputationRequest.getWorkspace()
            )
        );

        // 4. 평판 키워드 매핑 추가
        reputation.addReputationKeywordMap(keywords, keywordMap);

        // 5. 평판 요청 상태를 완료로 변경
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
