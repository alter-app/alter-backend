package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "공고 스케줄 생성 요청 DTO")
public class CreatePostingScheduleRequestDto {

    @Schema(description = "근무 요일", example = "['MONDAY', 'WEDNESDAY']")
    private List<DayOfWeek> workingDays;

    @Schema(description = "시작 시간", example = "09:00")
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "18:00")
    private LocalTime endTime;

    @Schema(description = "필요 인원", example = "3")
    private int positionsNeeded;

    @Schema(description = "아르바이트 포지션", example = "홀서빙")
    private String position;

}
