package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springdoc.core.annotations.ParameterObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "스케줄 조회 요청")
public class WorkScheduleInquiryRequestDto {

    @Parameter(description = "조회할 연도", example = "2024", required = true)
    private int year;

    @Parameter(description = "조회할 월", example = "1", required = true)
    private int month;
}
