package com.dreamteam.alter.adapter.inbound.common.dto;

import com.dreamteam.alter.domain.workspace.entity.BusinessType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(title = "업종(BusinessType) 응답 DTO")
public class BusinessTypeResponseDto {

    @NotNull
    @Schema(title = "업종 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(title = "업종 이름", example = "카페")
    private String name;

    @Schema(title = "상세 입력 요구 여부 ('기타' 업종이면 true)", example = "false")
    private boolean requiresDetail;

    @Schema(title = "업종 설명", example = "마스터에 없는 업종")
    private String description;

    public static BusinessTypeResponseDto from(BusinessType businessType) {
        return new BusinessTypeResponseDto(
            businessType.getId(),
            businessType.getName(),
            businessType.isRequiresDetail(),
            businessType.getDescription()
        );
    }
}
