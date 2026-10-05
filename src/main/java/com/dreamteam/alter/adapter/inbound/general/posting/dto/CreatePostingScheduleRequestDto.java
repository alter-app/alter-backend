package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.domain.posting.command.PostingScheduleCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    @NotEmpty
    private List<DayOfWeek> workingDays;

    @Schema(description = "시작 시간", example = "09:00")
    @NotNull
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "18:00")
    @NotNull
    private LocalTime endTime;

    @Schema(description = "아르바이트 포지션", example = "홀서빙")
    @NotBlank
    private String position;

    public PostingScheduleCommand toCommand() {
        return new PostingScheduleCommand(workingDays, startTime, endTime, position);
    }

}
