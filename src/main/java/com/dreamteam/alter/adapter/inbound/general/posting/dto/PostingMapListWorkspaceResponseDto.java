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
@Schema(description = "지도 공고 리스트 조회 업장 정보 응답 DTO")
public class PostingMapListWorkspaceResponseDto {

    @NotNull
    @Schema(description = "업장 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "업장 이름", example = "카페 알터")
    private String businessName;

    @NotNull
    @Schema(description = "위도", example = "37.5665")
    private BigDecimal latitude;

    @NotNull
    @Schema(description = "경도", example = "126.9780")
    private BigDecimal longitude;

    public static PostingMapListWorkspaceResponseDto from(Workspace entity) {
        return PostingMapListWorkspaceResponseDto.builder()
            .id(entity.getId())
            .businessName(entity.getBusinessName())
            .latitude(entity.getLatitude())
            .longitude(entity.getLongitude())
            .build();
    }
}
