package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestCommentListResponse;
import com.dreamteam.alter.domain.workspace.type.CommentOwner;

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
@Schema(description = "업장 등록 신청 댓글 조회 DTO")
public class WorkspaceRequestCommentResponseDto {

	@Schema(description = "댓글 ID", example = "1")
	private Long id;

	@Schema(description = "작성 유저 ID", example = "1")
	private Long userId;

	@Schema(description = "댓글 작성자 구분", example = "USER")
	private CommentOwner commentOwner;

	@Schema(description = "댓글 내용", example = "보완 자료 첨부드립니다.")
	private String comment;

	@Schema(description = "첨부파일 목록")
	private List<FileResponseDto> files;

	@Schema(description = "댓글 생성시각", example = "2026-03-01T10:00:00")
	private LocalDateTime createdAt;

	public static WorkspaceRequestCommentResponseDto from(
		WorkspaceRequestCommentListResponse entity,
		List<FileResponseDto> files
	) {
		return WorkspaceRequestCommentResponseDto.builder()
			.id(entity.getId())
			.userId(entity.getUserId())
			.commentOwner(entity.getCommentOwner())
			.comment(entity.getComment())
			.files(files)
			.createdAt(entity.getCreatedAt())
			.build();
	}
}
