package com.dreamteam.alter.adapter.inbound.admin.workspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "어드민 - 반려 사유 댓글 등록 DTO")
public class AdminCreateWorkspaceReasonCommentRequestDto {

	@Schema(description = "댓글 내용", example = "자료 보완하세요")
	@Size(max = 255)
	@NotBlank
	private String comment;
}
