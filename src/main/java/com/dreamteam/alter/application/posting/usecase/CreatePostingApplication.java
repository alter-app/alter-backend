package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingApplicationRequestDto;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.notification.NotificationMessageBuilder;
import com.dreamteam.alter.common.notification.NotificationMessageConstants;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.posting.entity.PostingSchedule;
import com.dreamteam.alter.domain.posting.port.inbound.CreatePostingApplicationUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationRepository;
import com.dreamteam.alter.domain.posting.port.outbound.PostingScheduleQueryRepository;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("createPostingApplication")
@RequiredArgsConstructor
@Transactional
public class CreatePostingApplication implements CreatePostingApplicationUseCase {

    private final PostingScheduleQueryRepository postingScheduleQueryRepository;
    private final PostingApplicationRepository postingApplicationRepository;
    private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;
    private final NotificationService notificationService;

    @Override
    public void execute(AppActor actor, Long postingId, CreatePostingApplicationRequestDto request) {
        PostingSchedule postingSchedule =
            postingScheduleQueryRepository.findByIdAndPostingId(postingId, request.getPostingScheduleId())
                .orElseThrow(() -> new CustomException(ErrorCode.POSTING_SCHEDULE_NOT_FOUND));

        if (workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(
                postingSchedule.getPosting().getWorkspace(),
                actor.getUser()
            )
            .isPresent()
        ) {
            throw new CustomException(ErrorCode.WORKSPACE_WORKER_ALREADY_EXISTS);
        }

        PostingApplication postingApplication = PostingApplication.create(
            postingSchedule, actor.getUser(), request.getDescription()
        );
        postingApplicationRepository.save(postingApplication);
        
        // 매니저에게 지원 알림 전송
        sendApplicationNotification(postingSchedule);
    }
    
    private void sendApplicationNotification(PostingSchedule postingSchedule) {
        try {
            String postingTitle = postingSchedule.getPosting().getTitle();
            Long managerUserId = postingSchedule.getPosting().getWorkspace().getManagerUser().getUser().getId();
            
            String title = NotificationMessageConstants.PostingApplication.NEW_APPLICATION_TITLE;
            String body = NotificationMessageBuilder.buildNewApplicationMessage(postingTitle);
            
            notificationService.sendNotification(
                FcmNotificationRequestDto.of(managerUserId, TokenScope.MANAGER, title, body)
            );
        } catch (CustomException e) {
            // 알림 발송 실패는 지원 프로세스에 영향을 주지 않음
        }
    }

}
