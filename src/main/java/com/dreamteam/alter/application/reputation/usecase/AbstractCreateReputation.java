package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationKeywordMapDto;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.reputation.entity.Reputation;
import com.dreamteam.alter.domain.reputation.entity.ReputationKeyword;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationKeywordQueryRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
public abstract class AbstractCreateReputation {

    protected final ReputationRequestRepository reputationRequestRepository;
    protected final ReputationRepository reputationRepository;
    protected final ReputationKeywordQueryRepository reputationKeywordQueryRepository;
    protected final NotificationService notificationService;

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

    protected final void createReputation(
        ReputationRequest reputationRequest,
        ReputationType fromType,
        Long fromId,
        ReputationType toType,
        Long toId,
        Workspace workspace,
        Set<ReputationKeywordMapDto> keywords,
        Map<String, ReputationKeyword> keywordMap
    ) {

        Reputation reputation = reputationRepository.save(
            Reputation.create(
                reputationRequest,
                fromType,
                fromId,
                toType,
                toId,
                workspace
            )
        );

        reputation.addReputationKeywordMap(keywords, keywordMap);
    }

    protected final void sendNotificationToTarget(Long targetUserId, String title, String body) {
        try {
            notificationService.sendNotification(
                FcmNotificationRequestDto.of(targetUserId, title, body)
            );
        } catch (CustomException e) {
            // 알림 발송 실패는 평판 요청 프로세스에 영향을 주지 않음
        }
    }
}
