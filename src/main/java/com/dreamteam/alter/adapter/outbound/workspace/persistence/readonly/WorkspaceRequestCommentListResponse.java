package com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly;

import java.time.LocalDateTime;

import com.dreamteam.alter.domain.workspace.type.CommentOwner;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceRequestCommentListResponse {

	private Long id;
	private Long workspaceRequestId;
	private Long userId;
	private CommentOwner commentOwner;
	private String comment;
	private LocalDateTime createdAt;
}
