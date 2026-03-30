package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import java.time.LocalDateTime;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceReasonComment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "업장 등록 요청 승인/반려 댓글 조회 DTO")
public class WorkspaceReasonCommentResponseDto {

	@Schema(description = "댓글 ID", example = "1")
	private Long id;

	@Schema(description = "댓글 내용", example = "자료 누락")
	private String comment;

	@Schema(description = "댓글 생성시각", example = "2026-03-01T10:00:00")
	private LocalDateTime createdAt;

	public static WorkspaceReasonCommentResponseDto from(WorkspaceReasonComment entity) {
		return new WorkspaceReasonCommentResponseDto(
			entity.getId(),
			entity.getComment(),
			entity.getCreatedAt()
		);
	}
}
