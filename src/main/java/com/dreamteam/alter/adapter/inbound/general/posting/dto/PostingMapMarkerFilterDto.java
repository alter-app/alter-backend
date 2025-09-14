package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.CoordinateDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springdoc.core.annotations.ParameterObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "지도 마커 조회 필터 DTO")
public class PostingMapMarkerFilterDto {

    @Parameter(description = "지도 좌측 상단 좌표")
    private CoordinateDto coordinate1;

    @Parameter(description = "지도 우측 하단 좌표")
    private CoordinateDto coordinate2;
}
