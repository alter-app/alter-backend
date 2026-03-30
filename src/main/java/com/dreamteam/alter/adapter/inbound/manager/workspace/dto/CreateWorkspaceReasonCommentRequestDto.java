package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매니저 - 승인/반려 사유 댓글 등록 DTO")
public class CreateWorkspaceReasonCommentRequestDto {

	@Schema(description = "댓글 내용", example = "왜 반려죠")
	@Size(max = 255)
	@NotBlank
	private String comment;
}
