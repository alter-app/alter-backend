package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;
import org.springdoc.core.annotations.ParameterObject;

import java.time.DateTimeException;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "스케줄 조회 요청")
public class WorkScheduleInquiryRequestDto {

    @Parameter(description = "조회할 연도")
    private Integer year;

    @Parameter(description = "조회할 월")
    private Integer month;

    @Parameter(description = "조회할 일 (일별 조회 시 사용)")
    private Integer day;

    @AssertTrue(message = "유효한 날짜를 입력해 주세요.")
    private boolean isDailyDateValid() {
        if (year == null || month == null || day == null) return true;
        try {
            LocalDate.of(year, month, day);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }
}
