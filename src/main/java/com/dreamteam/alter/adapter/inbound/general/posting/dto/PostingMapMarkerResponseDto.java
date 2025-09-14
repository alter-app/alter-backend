package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingListForMapMarkerResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "지도 마커 응답 DTO")
public class PostingMapMarkerResponseDto {

    @NotNull
    @Schema(description = "업장 ID", example = "1")
    private Long workspaceId;

    @NotNull
    @Schema(description = "위도", example = "37.5665")
    private BigDecimal latitude;

    @NotNull
    @Schema(description = "경도", example = "126.9780")
    private BigDecimal longitude;

    public static PostingMapMarkerResponseDto of(PostingListForMapMarkerResponse entity) {
        return PostingMapMarkerResponseDto.builder()
            .workspaceId(entity.getId())
            .latitude(entity.getLatitude())
            .longitude(entity.getLongitude())
            .build();
    }
}
