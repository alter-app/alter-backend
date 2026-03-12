package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.application.notification.FcmNotificationEvent;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.notification.NotificationMessageConstants;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.inbound.SendJoinRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessJoinRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessJoinRequestRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("sendJoinRequest")
@RequiredArgsConstructor
@Transactional
public class SendJoinRequest implements SendJoinRequestUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final BusinessJoinRequestQueryRepository businessJoinRequestQueryRepository;
    private final BusinessJoinRequestRepository businessJoinRequestRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void execute(AppActor actor, Long workspaceId) {
        Workspace workspace = workspaceQueryRepository.findById(workspaceId)
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        if (workspaceQueryRepository.isUserActiveWorkerInWorkspace(actor.getUser(), workspaceId)) {
            throw new CustomException(ErrorCode.WORKSPACE_WORKER_ALREADY_EXISTS);
        }

        if (businessJoinRequestQueryRepository.existsPendingRequest(workspace, actor.getUser())) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 합류 요청이 진행 중인 업장입니다.");
        }

        BusinessJoinRequest joinRequest = BusinessJoinRequest.create(workspace, actor.getUser());
        businessJoinRequestRepository.save(joinRequest);

        String title = NotificationMessageConstants.JoinRequest.REQUEST_RECEIVED_TITLE;
        String body = String.format(
            NotificationMessageConstants.JoinRequest.REQUEST_RECEIVED_BODY,
            actor.getUser().getName()
        );
        Long managerUserId = workspace.getManagerUser().getUser().getId();
        eventPublisher.publishEvent(
            new FcmNotificationEvent(FcmNotificationRequestDto.of(managerUserId, TokenScope.MANAGER, title, body))
        );
    }
}
