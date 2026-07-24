package com.dreamteam.alter.adapter.inbound.general.posting.dto;

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
@Schema(description = "공고 리스트 조회 업장 정보 응답 DTO")
public class PostingListWorkspaceResponseDto {

    @NotNull
    @Schema(description = "업장 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "업장 이름", example = "카페 알터")
    private String businessName;

    @NotBlank
    @Schema(description = "업종", example = "카페")
    private String businessType;

    @Schema(description = "업종 상세 (업종이 '기타'인 경우)", example = "떡볶이 전문점")
    private String businessTypeDetail;

    @NotBlank
    @Schema(description = "업장 시/도", example = "서울특별시")
    private String province;

    @NotBlank
    @Schema(description = "업장 시/군/구", example = "강남구")
    private String district;

    @NotBlank
    @Schema(description = "업장 읍/면/동", example = "역삼동")
    private String town;

    @NotNull
    @Schema(description = "위도", example = "37.5665")
    private BigDecimal latitude;

    @NotNull
    @Schema(description = "경도", example = "126.9780")
    private BigDecimal longitude;

    public static PostingListWorkspaceResponseDto from(Workspace entity) {
        return PostingListWorkspaceResponseDto.builder()
            .id(entity.getId())
            .businessName(entity.getBusinessName())
            .businessType(entity.getBusinessType().getName())
            .businessTypeDetail(entity.getBusinessTypeDetail())
            .province(entity.getProvince())
            .district(entity.getDistrict())
            .town(entity.getTown())
            .latitude(entity.getLatitude())
            .longitude(entity.getLongitude())
            .build();
    }

}
