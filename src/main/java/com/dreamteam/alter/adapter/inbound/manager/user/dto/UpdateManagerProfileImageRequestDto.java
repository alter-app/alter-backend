package com.dreamteam.alter.adapter.inbound.manager.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "프로필 이미지 수정 요청 DTO")
public class UpdateManagerProfileImageRequestDto {

	@NotBlank
	@Schema(description = "이미지 파일 ID", example = "01959b4e-4e5f-7c3a-8d9e-0f1a2b3c4d5e")
	private String fileId;
}
