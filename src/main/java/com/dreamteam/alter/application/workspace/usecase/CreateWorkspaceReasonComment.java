package com.dreamteam.alter.application.workspace.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceReasonCommentRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceReason;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceReasonComment;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceReasonCommentUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonCommentRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.type.CommentOwner;

import lombok.RequiredArgsConstructor;

@Service("createWorkspaceReasonComment")
@RequiredArgsConstructor
@Transactional
public class CreateWorkspaceReasonComment implements CreateWorkspaceReasonCommentUseCase {

    private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
    private final WorkspaceReasonQueryRepository workspaceReasonQueryRepository;
    private final WorkspaceReasonCommentRepository workspaceReasonCommentRepository;

    @Override
    public void execute(AppActor actor, Long workspaceRequestId, Long reasonId, CreateWorkspaceReasonCommentRequestDto request) {
        if (!workspaceRequestQueryRepository.existsByIdAndUserId(workspaceRequestId, actor.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        WorkspaceReason reason = workspaceReasonQueryRepository.findByIdAndWorkspaceRequestId(reasonId, workspaceRequestId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        workspaceReasonCommentRepository.save(WorkspaceReasonComment.create(reason, actor.getUser(), CommentOwner.USER, request.getComment()));
    }
}
