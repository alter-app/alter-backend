package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import com.dreamteam.alter.domain.workspace.entity.Workspace;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "공고 상세 조회 업장 정보 응답 DTO")
public class PostingDetailWorkspaceResponseDto {

    @NotNull
    @Schema(description = "업장 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "업장 이름", example = "카페 알터")
    private String name;

    @NotBlank
    @Schema(description = "업장 상세 주소", example = "서울특별시 강남구 테헤란로 123")
    private String fullAddress;

    @NotBlank
    @Schema(description = "업장 시/도", example = "서울")
    private String province;

    @NotBlank
    @Schema(description = "업장 시/군/구", example = "강남구")
    private String district;

    @NotBlank
    @Schema(description = "업장 읍/면/동", example = "역삼동")
    private String town;

    @NotNull
    @Schema(description = "업장 위도", example = "37.5665")
    private BigDecimal latitude;

    @NotNull
    @Schema(description = "업장 경도", example = "126.9780")
    private BigDecimal longitude;

    public static PostingDetailWorkspaceResponseDto from(Workspace workspace) {
        return PostingDetailWorkspaceResponseDto.builder()
            .id(workspace.getId())
            .name(workspace.getBusinessName())
            .fullAddress(workspace.getFullAddress())
            .province(workspace.getProvince())
            .district(workspace.getDistrict())
            .town(workspace.getTown())
            .latitude(workspace.getLatitude())
            .longitude(workspace.getLongitude())
            .build();
    }

}
