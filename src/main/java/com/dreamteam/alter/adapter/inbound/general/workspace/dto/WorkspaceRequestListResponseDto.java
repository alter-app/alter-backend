package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import java.time.LocalDateTime;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestListResponse;
import com.dreamteam.alter.domain.workspace.type.WorkspaceRequestStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "업장 등록 신청 목록 조회 응답 DTO")
public class WorkspaceRequestListResponseDto {

	@Schema(description = "업장 등록 신청 ID", example = "1")
	private Long id;

	@Schema(description = "업장 이름", example = "세븐일레븐")
	private String businessName;

	@Schema(description = "업장 전체 주소", example = "서울특별시 강남구 테헤란로 123")
	private String fullAddress;

	@Schema(description = "등록일", example = "2026-03-01T10:00:00")
	private LocalDateTime createdAt;

	@Schema(description = "업장 등록 신청 상태")
	private DescribedEnumDto<WorkspaceRequestStatus> status;

	public static WorkspaceRequestListResponseDto of(WorkspaceRequestListResponse entity) {
		return WorkspaceRequestListResponseDto.builder()
			.id(entity.getId())
			.businessName(entity.getBusinessName())
			.fullAddress(entity.getFullAddress())
			.createdAt(entity.getCreatedAt())
			.status(DescribedEnumDto.of(entity.getStatus(), WorkspaceRequestStatus.describe()))
			.build();
	}
}
