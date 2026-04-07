package com.dreamteam.alter.adapter.inbound.admin.workspace.dto;

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
@Schema(description = "어드민 업장 등록 신청 상세 조회 응답 DTO")
public class AdminWorkspaceRequestResponseDto {

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

	@Schema(description = "사업자등록증명원 파일 URL (S3)")
	private String workspaceCertFileId;

	@Schema(description = "대표자 신분증 사본 파일 URL (S3)")
	private String workspaceOwnIdentityFileId;

	@Schema(description = "위임 확인서 파일 URL (S3)")
	private String workspaceWarrantFileId;

	@Schema(description = "업장 등록 신청 일시", example = "2023-10-01T12:00:00")
	private LocalDateTime createdAt;

	@Schema(description = "업장 등록 신청 수정 일시", example = "2023-10-01T12:00:00")
	private LocalDateTime updatedAt;

	public static AdminWorkspaceRequestResponseDto from(WorkspaceRequestResponse entity) {
		return AdminWorkspaceRequestResponseDto.builder()
			.id(entity.getId())
			.businessRegistrationNo(entity.getBusinessRegistrationNo())
			.businessName(entity.getBusinessName())
			.businessType(entity.getBusinessType())
			.contact(entity.getContact())
			.status(DescribedEnumDto.of(entity.getStatus(), WorkspaceRequestStatus.describe()))
			.fullAddress(entity.getFullAddress())
			.latitude(entity.getLatitude())
			.longitude(entity.getLongitude())
			.workspaceCertFileId(entity.getWorkspaceCertFileUrl())
			.workspaceOwnIdentityFileId(entity.getWorkspaceOwnIdentityFileUrl())
			.workspaceWarrantFileId(entity.getWorkspaceWarrantFileUrl())
			.createdAt(entity.getCreatedAt())
			.updatedAt(entity.getUpdatedAt())
			.build();
	}
}
