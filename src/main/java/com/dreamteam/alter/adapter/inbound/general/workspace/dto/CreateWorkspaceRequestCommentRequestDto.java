package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업장 등록 신청 댓글 등록 DTO")
public class CreateWorkspaceRequestCommentRequestDto {

	@Schema(description = "댓글 내용", example = "보완 자료 첨부드립니다.")
	@Size(max = 255)
	@NotBlank
	private String comment;

	@Schema(description = "첨부파일 ID 목록", example = "[\"01959b4e-4e5f-7c3a-8d9e-0f1a2b3c4d5e\"]")
	private List<String> fileIds;
}
