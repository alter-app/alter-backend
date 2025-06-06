package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.domain.posting.entity.PostingSchedule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "사용자 공고 지원 스케줄 요약 응답 DTO")
public class UserPostingApplicationPostingScheduleSummaryResponseDto {

    @NotNull
    @Schema(description = "스케줄 ID", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "근무 요일", example = "['MONDAY', 'WEDNESDAY']")
    private List<DayOfWeek> workingDays;

    @NotNull
    @Schema(description = "시작 시간", example = "09:00")
    private LocalTime startTime;

    @NotNull
    @Schema(description = "종료 시간", example = "18:00")
    private LocalTime endTime;

    @NotBlank
    @Schema(description = "아르바이트 포지션", example = "홀서빙")
    private String position;

    public static UserPostingApplicationPostingScheduleSummaryResponseDto from(PostingSchedule entity) {
        return UserPostingApplicationPostingScheduleSummaryResponseDto.builder()
            .id(entity.getId())
            .workingDays(entity.getWorkingDays())
            .startTime(entity.getStartTime())
            .endTime(entity.getEndTime())
            .position(entity.getPosition())
            .build();
    }

}
