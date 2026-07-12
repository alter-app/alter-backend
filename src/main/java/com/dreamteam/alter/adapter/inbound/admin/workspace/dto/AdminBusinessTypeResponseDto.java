package com.dreamteam.alter.adapter.inbound.admin.workspace.dto;

import com.dreamteam.alter.domain.workspace.entity.BusinessType;

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
@Schema(description = "업종(BusinessType) 응답 DTO")
public class AdminBusinessTypeResponseDto {

    @Schema(description = "업종 ID", example = "1")
    private Long id;

    @Schema(description = "업종명", example = "카페")
    private String name;

    @Schema(description = "업종 설명", example = "카페/디저트 매장")
    private String description;

    @Schema(description = "상세 입력 요구 여부 ('기타' 업종이면 true, 삭제 불가)", example = "false")
    private boolean requiresDetail;

    public static AdminBusinessTypeResponseDto from(BusinessType businessType) {
        return AdminBusinessTypeResponseDto.builder()
            .id(businessType.getId())
            .name(businessType.getName())
            .description(businessType.getDescription())
            .requiresDetail(businessType.isRequiresDetail())
            .build();
    }

    // 관리자 생성 업종은 항상 requiresDetail=false 이므로 재조회 없이 생성 결과를 구성한다.
    public static AdminBusinessTypeResponseDto of(Long id, String name, String description) {
        return AdminBusinessTypeResponseDto.builder()
            .id(id)
            .name(name)
            .description(description)
            .requiresDetail(false)
            .build();
    }
}
