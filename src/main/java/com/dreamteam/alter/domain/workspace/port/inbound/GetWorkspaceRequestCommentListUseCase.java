package com.dreamteam.alter.domain.workspace.port.inbound;

import java.util.List;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestCommentResponseDto;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.type.CommentOwner;

public interface GetWorkspaceRequestCommentListUseCase {
	List<WorkspaceRequestCommentResponseDto> execute(Long workspaceRequestId, User requester, CommentOwner ownerType);
}
