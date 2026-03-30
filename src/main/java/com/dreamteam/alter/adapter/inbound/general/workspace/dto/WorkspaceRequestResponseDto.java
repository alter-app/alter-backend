package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestResponse;
import com.dreamteam.alter.domain.workspace.type.WorkspaceRequestStatus;

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
@Schema(description = "업장 등록 신청 상세 조회 응답 DTO")
public class WorkspaceRequestResponseDto {

	@Schema(description = "업장 등록 신청 ID", example = "1")
	private Long id;

	@Schema(description = "사업자 등록번호", example = "123-45-67890")
	private String businessRegistrationNo;

	@Schema(description = "업장 이름", example = "드림팀 카페")
	private String businessName;

	@Schema(description = "업종", example = "카페")
	private String businessType;

	@Schema(description = "연락처", example = "01012345678")
	private String contact;

	@Schema(description = "업장 등록 신청 상태")
	private DescribedEnumDto<WorkspaceRequestStatus> status;

	@Schema(description = "업장 전체 주소", example = "서울특별시 강남구 테헤란로 123")
	private String fullAddress;

	@Schema(description = "업장 위도", example = "37.5665")
	private BigDecimal latitude;

	@Schema(description = "업장 경도", example = "126.9780")
	private BigDecimal longitude;

	@Schema(description = "업장 등록 신청 일시", example = "2023-10-01T12:00:00")
	private LocalDateTime createdAt;

	@Schema(description = "업장 등록 신청 수정 일시", example = "2023-10-01T12:00:00")
	private LocalDateTime updatedAt;

	public static WorkspaceRequestResponseDto of(WorkspaceRequestResponse workspaceRequest) {
		return WorkspaceRequestResponseDto.builder()
			.id(workspaceRequest.getId())
			.businessRegistrationNo(workspaceRequest.getBusinessRegistrationNo())
			.businessName(workspaceRequest.getBusinessName())
			.businessType(workspaceRequest.getBusinessType())
			.contact(workspaceRequest.getContact())
			.status(DescribedEnumDto.of(workspaceRequest.getStatus(), WorkspaceRequestStatus.describe()))
			.fullAddress(workspaceRequest.getFullAddress())
			.latitude(workspaceRequest.getLatitude())
			.longitude(workspaceRequest.getLongitude())
			.createdAt(workspaceRequest.getCreatedAt())
			.updatedAt(workspaceRequest.getUpdatedAt())
			.build();
	}
}
