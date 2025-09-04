package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceResponse;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "매니저 - 업장 상세 조회 응답 DTO")
public class ManagerWorkspaceResponseDto {

    @NotNull
    @Schema(description = "업장 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "사업자 등록번호", example = "123-45-67890")
    private String businessRegistrationNo;

    @NotBlank
    @Schema(description = "업장 이름", example = "드림팀 카페")
    private String businessName;

    @NotBlank
    @Schema(description = "업종", example = "카페")
    private String businessType;

    @NotBlank
    @Schema(description = "연락처", example = "010-1234-5678")
    private String contact;

    @NotBlank
    @Schema(description = "업장 설명", example = "아늑한 분위기의 카페입니다.")
    private String description;

    @NotNull
    @Schema(description = "업장 등록 상태")
    private DescribedEnumDto<WorkspaceStatus> status;

    @NotBlank
    @Schema(description = "업장 전체 주소", example = "서울특별시 강남구 테헤란로 123")
    private String fullAddress;

    @NotNull
    @Schema(description = "업장 위도", example = "37.5665")
    private BigDecimal latitude;

    @NotNull
    @Schema(description = "업장 경도", example = "126.9780")
    private BigDecimal longitude;

    @NotNull
    @Schema(description = "업장 등록 일시", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    public static ManagerWorkspaceResponseDto of(ManagerWorkspaceResponse entity) {
        return ManagerWorkspaceResponseDto.builder()
            .id(entity.getId())
            .businessRegistrationNo(entity.getBusinessRegistrationNo())
            .businessName(entity.getBusinessName())
            .businessType(entity.getBusinessType())
            .contact(entity.getContact())
            .description(entity.getDescription())
            .status(DescribedEnumDto.of(entity.getStatus(), WorkspaceStatus.describe()))
            .fullAddress(entity.getFullAddress())
            .latitude(entity.getLatitude())
            .longitude(entity.getLongitude())
            .createdAt(entity.getCreatedAt())
            .build();
    }

}
