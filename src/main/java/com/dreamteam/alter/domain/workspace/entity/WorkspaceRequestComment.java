package com.dreamteam.alter.domain.workspace.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.type.CommentOwner;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "workspace_request_comments")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class WorkspaceRequestComment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JoinColumn(name = "workspace_request_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private WorkspaceRequest workspaceRequest;

	@JoinColumn(name = "user_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "comment_owner", length = 50, nullable = false)
	private CommentOwner commentOwner;

	@Column(name = "comment", nullable = false)
	private String comment;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public static WorkspaceRequestComment create(
		WorkspaceRequest workspaceRequest,
		User user,
		CommentOwner commentOwner,
		String comment
	) {
		return WorkspaceRequestComment.builder()
			.workspaceRequest(workspaceRequest)
			.user(user)
			.commentOwner(commentOwner)
			.comment(comment)
			.build();
	}
}
