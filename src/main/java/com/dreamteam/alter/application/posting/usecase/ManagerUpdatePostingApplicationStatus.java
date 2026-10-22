package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingApplicationStatusRequestDto;
import com.dreamteam.alter.application.notification.FcmNotificationEvent;
import com.dreamteam.alter.common.notification.NotificationMessageConstants;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.type.NotificationType;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.posting.port.inbound.ManagerUpdatePostingApplicationStatusUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceWorkerUseCase;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service("managerUpdatePostingApplicationStatus")
@RequiredArgsConstructor
@Transactional
public class ManagerUpdatePostingApplicationStatus implements ManagerUpdatePostingApplicationStatusUseCase {

    private final PostingApplicationQueryRepository postingApplicationQueryRepository;
    private final PostingQueryRepository postingQueryRepository;
    private final EntityManager entityManager;
    private final ApplicationEventPublisher eventPublisher;

    @Resource(name = "addWorkerToWorkspace")
    private final CreateWorkspaceWorkerUseCase createWorkspaceWorker;

    @Override
    public void execute(
        Long postingApplicationId,
        UpdatePostingApplicationStatusRequestDto request,
        ManagerActor actor
    ) {
        // SHORTLISTED, ACCEPTED, REJECTED 상태로만 변경 가능
        switch (request.getStatus()) {
            case SHORTLISTED, ACCEPTED, REJECTED -> {}
            default -> throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        ManagerUser managerUser = actor.getManagerUser();

        Long postingId = postingApplicationQueryRepository
            .findPostingIdByManagerAndApplicationId(managerUser, postingApplicationId)
            .orElseThrow(() -> new CustomException(ErrorCode.POSTING_APPLICATION_NOT_FOUND));

        PostingApplication postingApplication;
        try {
            postingQueryRepository.findByManagerAndIdWithPessimisticLock(postingId, managerUser)
                .orElseThrow(() -> new CustomException(ErrorCode.POSTING_NOT_FOUND));

            // 공고 잠금을 얻은 뒤 지원서를 조회해 대기 중 변경된 상태를 확인한다.
            postingApplication = postingApplicationQueryRepository.getByManagerAndId(managerUser, postingApplicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.POSTING_APPLICATION_NOT_FOUND));

            // ACCEPTED, CANCELLED, REJECTED, EXPIRED 상태의 지원서는 상태 변경 불가
            switch (postingApplication.getStatus()) {
                case ACCEPTED, CANCELLED, REJECTED, EXPIRED ->
                    throw new CustomException(ErrorCode.POSTING_APPLICATION_STATUS_NOT_UPDATABLE);
                default -> {}
            }

            postingApplication.updateStatus(request.getStatus());

            if (request.getStatus().equals(PostingApplicationStatus.ACCEPTED)) {
                createWorkspaceWorker.execute(
                    postingApplication.getPosting().getWorkspace(),
                    postingApplication.getUser()
                );
            }

            // 지원서 UPDATE의 lock_timeout도 커밋 전에 기존 오류 코드로 변환한다.
            entityManager.flush();
        } catch (PessimisticLockingFailureException | LockTimeoutException | PessimisticLockException e) {
            throw new CustomException(ErrorCode.TOO_MANY_REQUESTS);
        }

        // 커밋 후 별도 트랜잭션에서 지원자에게 상태 변경 알림 전송
        sendApplicationStatusNotification(postingApplication, request.getStatus());
    }

    private void sendApplicationStatusNotification(
        PostingApplication postingApplication,
        PostingApplicationStatus status
    ) {
        try {
            String businessName = postingApplication.getPosting()
                .getWorkspace()
                .getBusinessName();
            Long applicantUserId = postingApplication.getUser()
                .getId();

            String title;
            String body;

            switch (status) {
                case SHORTLISTED -> {
                    title = NotificationMessageConstants.PostingApplication.SHORTLISTED_TITLE;
                    body = NotificationMessageConstants.PostingApplication.SHORTLISTED_BODY.formatted(businessName);
                }
                case ACCEPTED -> {
                    title = NotificationMessageConstants.PostingApplication.ACCEPTED_TITLE;
                    body = NotificationMessageConstants.PostingApplication.ACCEPTED_BODY.formatted(businessName);
                }
                case REJECTED -> {
                    title = NotificationMessageConstants.PostingApplication.REJECTED_TITLE;
                    body = NotificationMessageConstants.PostingApplication.REJECTED_BODY.formatted(businessName);
                }
                default -> throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "지원하지 않는 상태입니다.");
            }

            eventPublisher.publishEvent(new FcmNotificationEvent(
                FcmNotificationRequestDto.of(applicantUserId, TokenScope.APP, NotificationType.POSTING_APPLICATION, title, body)
            ));
        } catch (CustomException e) {
            // 알림 발송 실패는 상태 변경 프로세스에 영향을 주지 않음
        }
    }

}
