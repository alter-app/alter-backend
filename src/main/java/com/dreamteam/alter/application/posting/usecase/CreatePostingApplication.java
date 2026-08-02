package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingApplicationRequestDto;
import com.dreamteam.alter.application.notification.FcmNotificationEvent;
import com.dreamteam.alter.common.notification.NotificationMessageBuilder;
import com.dreamteam.alter.common.notification.NotificationMessageConstants;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.type.NotificationType;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.posting.entity.PostingSchedule;
import com.dreamteam.alter.domain.posting.port.inbound.CreatePostingApplicationUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationRepository;
import com.dreamteam.alter.domain.posting.port.outbound.PostingScheduleQueryRepository;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service("createPostingApplication")
@RequiredArgsConstructor
@Transactional
public class CreatePostingApplication implements CreatePostingApplicationUseCase {

    private final PostingScheduleQueryRepository postingScheduleQueryRepository;
    private final PostingQueryRepository postingQueryRepository;
    private final PostingApplicationRepository postingApplicationRepository;
    private final PostingApplicationQueryRepository postingApplicationQueryRepository;
    private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;
    private final ApplicationEventPublisher eventPublisher;

    // TODO: postingId, postingScheduleId 둘 다 인자로 받아 확인하도록 수정 필요
    @Override
    public void execute(AppActor actor, Long postingId, CreatePostingApplicationRequestDto request) {
        Posting posting = postingQueryRepository.findByIdWithPessimisticLock(postingId)
            .orElseThrow(() -> new CustomException(ErrorCode.POSTING_NOT_FOUND));

        PostingSchedule postingSchedule =
            postingScheduleQueryRepository.findByIdAndPostingId(postingId, request.getPostingScheduleId())
                .orElseThrow(() -> new CustomException(ErrorCode.POSTING_SCHEDULE_NOT_FOUND));

        if (!PostingStatus.OPEN.equals(posting.getStatus())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "모집이 종료된 공고입니다.");
        }

        if (workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(
                posting.getWorkspace(),
                actor.getUser()
            )
            .isPresent()
        ) {
            throw new CustomException(ErrorCode.WORKSPACE_WORKER_ALREADY_EXISTS);
        }

        // 같은 공고의 다른 스케줄에 이미 지원한 경우도 중복으로 본다
        if (postingApplicationQueryRepository.existsActiveByPostingIdAndUser(posting.getId(), actor.getUser())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "이미 지원한 공고입니다.");
        }

        PostingApplication postingApplication = PostingApplication.create(
            postingSchedule, actor.getUser(), request.getDescription()
        );
        postingApplicationRepository.save(postingApplication);

        publishApplicationNotification(posting);
    }

    private void publishApplicationNotification(Posting posting) {
        String postingTitle = posting.getTitle();
        Long managerUserId = posting.getWorkspace().getManagerUser().getUser().getId();

        String title = NotificationMessageConstants.PostingApplication.NEW_APPLICATION_TITLE;
        String body = NotificationMessageBuilder.buildNewApplicationMessage(postingTitle);

        eventPublisher.publishEvent(
            new FcmNotificationEvent(
                FcmNotificationRequestDto.of(
                    managerUserId,
                    TokenScope.MANAGER,
                    NotificationType.POSTING_APPLICATION,
                    title,
                    body
                )
            )
        );
    }

}
