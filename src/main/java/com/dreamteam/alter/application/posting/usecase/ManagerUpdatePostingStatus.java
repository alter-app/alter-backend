package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingStatusRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.application.notification.FcmNotificationEvent;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

@Service("managerUpdatePostingStatus")
@RequiredArgsConstructor
@Transactional
public class ManagerUpdatePostingStatus implements ManagerUpdatePostingStatusUseCase {

    private final PostingQueryRepository postingQueryRepository;
    private final PostingApplicationQueryRepository postingApplicationQueryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void execute(Long postingId, UpdatePostingStatusRequestDto request, ManagerActor actor) {
        ManagerUser managerUser = actor.getManagerUser();

        Posting posting;
        try {
            posting = postingQueryRepository.findByManagerAndIdWithPessimisticLock(postingId, managerUser)
                .orElseThrow(() -> new CustomException(ErrorCode.POSTING_NOT_FOUND));
        } catch (PessimisticLockingFailureException e) {
            throw new CustomException(ErrorCode.TOO_MANY_REQUESTS);
        }

        posting.updateStatus(request.getStatus());

        if (request.getStatus() == PostingStatus.CLOSED || request.getStatus() == PostingStatus.CANCELLED) {
            String body = NotificationMessageConstants.PostingApplication.REJECTED_BODY
                .formatted(posting.getWorkspace().getBusinessName());
            for (PostingApplication application : postingApplicationQueryRepository.findPendingByPostingIdWithUser(postingId)) {
                application.updateStatus(PostingApplicationStatus.REJECTED);
                eventPublisher.publishEvent(new FcmNotificationEvent(FcmNotificationRequestDto.of(
                    application.getUser().getId(), TokenScope.APP, NotificationType.POSTING_APPLICATION,
                    NotificationMessageConstants.PostingApplication.REJECTED_TITLE, body)));
            }
        }
    }
}
