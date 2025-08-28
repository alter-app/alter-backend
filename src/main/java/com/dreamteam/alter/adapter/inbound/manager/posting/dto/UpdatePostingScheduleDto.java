package com.dreamteam.alter.adapter.inbound.manager.posting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "스케줄 수정 DTO")
public class UpdatePostingScheduleDto {

    @NotNull
    @Schema(description = "스케줄 ID", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "근무일", example = "[\"MONDAY\", \"WEDNESDAY\"]")
    private List<String> workingDays;

    @NotNull
    @Schema(description = "시작 시간", example = "09:00")
    private String startTime;

    @NotNull
    @Schema(description = "종료 시간", example = "18:00")
    private String endTime;

    @NotNull
    @Schema(description = "필요 인원", example = "3")
    private int positionsNeeded;

    @NotBlank
    @Schema(description = "포지션", example = "홀서빙")
    private String position;
}
