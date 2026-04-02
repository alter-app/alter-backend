package com.dreamteam.alter.adapter.inbound.admin.workspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업장 등록 신청 반려 생성 DTO")
public class CreateWorkspaceReasonDto {

	@NotBlank
	@Max(value = 255)
	@Schema(description = "사유", example = "사업자등록증 기간 지남")
	private String reason;
}
