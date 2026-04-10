package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import java.time.LocalDateTime;

import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceReasonListResponse;

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
@Schema(description = "업장 등록 신청 반려 사유 응답 DTO")
public class WorkspaceReasonResponseDto {

	@Schema(description = "반려 사유 ID", example = "1")
	private Long id;

	@Schema(description = "반려 사유", example = "서류 누락")
	private String reason;

	@Schema(description = "작성 일시", example = "2026-03-01T10:00:00")
	private LocalDateTime createdAt;

	public static WorkspaceReasonResponseDto from(WorkspaceReasonListResponse entity) {
		return WorkspaceReasonResponseDto.builder()
			.id(entity.getId())
			.reason(entity.getReason())
			.createdAt(entity.getCreatedAt())
			.build();
	}
}
