package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.application.notification.FcmNotificationEvent;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.notification.NotificationMessageConstants;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.inbound.ApproveJoinRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceWorkerUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessJoinRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("approveJoinRequest")
@RequiredArgsConstructor
@Transactional
public class ApproveJoinRequest implements ApproveJoinRequestUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final BusinessJoinRequestQueryRepository businessJoinRequestQueryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Resource(name = "addWorkerToWorkspace")
    private CreateWorkspaceWorkerUseCase addWorkerToWorkspace;

    @Override
    public void execute(ManagerActor actor, Long workspaceId, Long requestId) {
        Workspace workspace = workspaceQueryRepository.findById(workspaceId)
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 업장의 관리자가 아닙니다.");
        }

        BusinessJoinRequest joinRequest = businessJoinRequestQueryRepository.findById(requestId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 합류 요청입니다."));

        if (!joinRequest.getWorkspace().getId().equals(workspaceId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 업장의 합류 요청이 아닙니다.");
        }

        joinRequest.approve();

        addWorkerToWorkspace.execute(workspace, joinRequest.getUser());

        String title = NotificationMessageConstants.JoinRequest.REQUEST_APPROVED_TITLE;
        String body = String.format(
            NotificationMessageConstants.JoinRequest.REQUEST_APPROVED_BODY,
            workspace.getBusinessName()
        );
        eventPublisher.publishEvent(
            new FcmNotificationEvent(FcmNotificationRequestDto.of(joinRequest.getUser().getId(), TokenScope.APP, title, body))
        );
    }
}
