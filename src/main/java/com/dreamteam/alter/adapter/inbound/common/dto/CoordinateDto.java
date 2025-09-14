package com.dreamteam.alter.adapter.inbound.common.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springdoc.core.annotations.ParameterObject;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "좌표 정보 DTO")
public class CoordinateDto {

    @Parameter(description = "위도", example = "37.5665")
    private BigDecimal latitude;

    @Parameter(description = "경도", example = "126.9780")
    private BigDecimal longitude;

}
