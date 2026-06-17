package com.dreamteam.alter.adapter.inbound.admin.workspace.dto;

import com.dreamteam.alter.domain.workspace.type.WorkspaceRequestStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업장 등록 신청 상태 변경 DTO")
public class UpdateWorkspaceRequestStatusDto {

	@NotNull
	@Schema(description = "변경할 상태 (ACTIVATED: 승인, REVOKED: 반려)", example = "ACTIVATED")
	private WorkspaceRequestStatus status;
}
