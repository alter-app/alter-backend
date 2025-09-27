package com.dreamteam.alter.adapter.inbound.manager.schedule.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springdoc.core.annotations.ParameterObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "매니저 스케줄 조회 요청")
public class ManagerWorkScheduleInquiryRequestDto {

    @Parameter(description = "업장 ID", example = "1", required = true)
    private Long workspaceId;

    @Parameter(description = "조회할 연도", example = "2024", required = true)
    private int year;

    @Parameter(description = "조회할 월", example = "1", required = true)
    private int month;
}
