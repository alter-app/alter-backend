package com.dreamteam.alter.application.workspace.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.CreateWorkspaceReasonCommentRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceReason;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceReasonComment;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceReasonCommentUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonCommentRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("createWorkspaceReasonComment")
@RequiredArgsConstructor
@Transactional
public class CreateWorkspaceReasonComment implements CreateWorkspaceReasonCommentUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final WorkspaceReasonQueryRepository workspaceReasonQueryRepository;
    private final WorkspaceReasonCommentRepository workspaceReasonCommentRepository;

    @Override
    public void execute(ManagerActor actor, Long workspaceId, Long reasonId, CreateWorkspaceReasonCommentRequestDto request) {
        if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        WorkspaceReason reason = workspaceReasonQueryRepository.findByIdAndWorkspaceId(reasonId, workspaceId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        WorkspaceReasonComment comment = WorkspaceReasonComment.create(reason, request.getComment());
        workspaceReasonCommentRepository.save(comment);
    }
}
