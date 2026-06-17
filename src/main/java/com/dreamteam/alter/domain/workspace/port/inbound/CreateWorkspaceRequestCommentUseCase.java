package com.dreamteam.alter.domain.workspace.port.inbound;

import java.util.List;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.type.CommentOwner;

public interface CreateWorkspaceRequestCommentUseCase {
	void execute(User user, Long workspaceRequestId, CommentOwner commentOwner, String comment, List<String> fileIds);
}
