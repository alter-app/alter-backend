package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingStatusRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.FcmBatchNotificationRequestDto;
import com.dreamteam.alter.application.notification.FcmBatchNotificationEvent;
import com.dreamteam.alter.common.notification.NotificationMessageConstants;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.type.NotificationType;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.port.inbound.ManagerUpdatePostingStatusUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("managerUpdatePostingStatus")
@RequiredArgsConstructor
@Transactional
public class ManagerUpdatePostingStatus implements ManagerUpdatePostingStatusUseCase {

    private final PostingQueryRepository postingQueryRepository;
    private final PostingApplicationQueryRepository postingApplicationQueryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;

    @Override
    public void execute(Long postingId, UpdatePostingStatusRequestDto request, ManagerActor actor) {
        ManagerUser managerUser = actor.getManagerUser();

        try {
            Posting posting = postingQueryRepository.findByManagerAndIdWithPessimisticLock(postingId, managerUser)
                .orElseThrow(() -> new CustomException(ErrorCode.POSTING_NOT_FOUND));

            posting.updateStatus(request.getStatus());

            List<Long> targetUserIds = new ArrayList<>();
            if (request.getStatus() == PostingStatus.CLOSED || request.getStatus() == PostingStatus.CANCELLED
                || request.getStatus() == PostingStatus.DELETED) {
                for (PostingApplication application : postingApplicationQueryRepository.findPendingByPostingIdWithUser(postingId)) {
                    application.updateStatus(PostingApplicationStatus.REJECTED);
                    targetUserIds.add(application.getUser().getId());
                }
            }

            // 커밋 시점까지 미루지 않고 잠금 예외를 여기서 변환한다.
            entityManager.flush();

            if (!targetUserIds.isEmpty()) {
                String body = NotificationMessageConstants.PostingApplication.REJECTED_BODY
                    .formatted(posting.getWorkspace().getBusinessName());
                eventPublisher.publishEvent(new FcmBatchNotificationEvent(FcmBatchNotificationRequestDto.of(
                    targetUserIds.stream().distinct().toList(), TokenScope.APP, NotificationType.POSTING_APPLICATION,
                    NotificationMessageConstants.PostingApplication.REJECTED_TITLE, body)));
            }
        } catch (PessimisticLockingFailureException | LockTimeoutException | PessimisticLockException e) {
            throw new CustomException(ErrorCode.TOO_MANY_REQUESTS);
        }
    }
}
