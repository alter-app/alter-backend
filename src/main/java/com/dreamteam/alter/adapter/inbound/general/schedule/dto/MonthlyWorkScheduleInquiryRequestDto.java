package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "월별 스케줄 조회 요청")
public class MonthlyWorkScheduleInquiryRequestDto {

    @Parameter(description = "조회할 연도", required = true, example = "2024")
    private Integer year;

    @Parameter(description = "조회할 월", required = true, example = "5")
    private Integer month;
}
