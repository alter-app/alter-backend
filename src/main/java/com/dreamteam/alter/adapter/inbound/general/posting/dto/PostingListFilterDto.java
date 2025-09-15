package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "사용자 공고 목록 조회 필터 DTO")
public class PostingListFilterDto {

    @Parameter(description = "시/도 (province)")
    private String province;

    @Parameter(description = "시/군/구 (district)")
    private String district;

    @Parameter(description = "읍/면/동 (town)")
    private String town;

    @Parameter(description = "최소 급여")
    private Integer minPayAmount;

    @Parameter(description = "최대 급여")
    private Integer maxPayAmount;

    @Parameter(description = "근무 시작 시간")
    private LocalTime startTime;

    @Parameter(description = "근무 종료 시간")
    private LocalTime endTime;

    @Parameter(description = "급여순 정렬 여부")
    private Boolean payAmountSort;
}
