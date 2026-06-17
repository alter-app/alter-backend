package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequestComment;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceRequestCommentUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestCommentRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.type.CommentOwner;

import lombok.RequiredArgsConstructor;

@Service("createWorkspaceRequestComment")
@RequiredArgsConstructor
@Transactional
public class CreateWorkspaceRequestComment implements CreateWorkspaceRequestCommentUseCase {

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
	private final WorkspaceRequestCommentRepository workspaceRequestCommentRepository;
	private final AttachFilesUseCase attachFiles;

	@Override
	public void execute(User user, Long workspaceRequestId, CommentOwner commentOwner, String comment, List<String> fileIds) {
		WorkspaceRequest workspaceRequest = workspaceRequestQueryRepository.findByIdWithUser(workspaceRequestId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 업장 등록 신청을 찾을 수 없습니다."));

		if (commentOwner == CommentOwner.USER && !workspaceRequest.getUser().getId().equals(user.getId())) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}

		WorkspaceRequestComment saved = workspaceRequestCommentRepository.save(
			WorkspaceRequestComment.create(workspaceRequest, user, commentOwner, comment)
		);

		if (fileIds != null && !fileIds.isEmpty()) {
			attachFiles.execute(fileIds, FileTargetType.WORKSPACE_REQUEST_COMMENT, saved.getId().toString(), user.getId());
		}
	}
}
