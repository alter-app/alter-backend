package com.dreamteam.alter.adapter.inbound.manager.posting.dto;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.ObjectUtils;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingScheduleRequestDto;
import com.dreamteam.alter.domain.posting.command.UpdatePostingCommand;
import com.dreamteam.alter.domain.posting.type.PaymentType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "매니저 공고 수정 요청 DTO")
public class UpdatePostingRequestDto {

    @NotBlank
    @Schema(description = "공고 제목", example = "홀서빙 구합니다")
    private String title;

    @NotBlank
    @Schema(description = "공고 설명", example = "홀서빙 구합니다. 주말 근무 가능하신 분 우대합니다.")
    private String description;

    @Positive
    @Schema(description = "급여", example = "10000")
    private int payAmount;

    @NotNull
    @Schema(description = "급여 타입", example = "HOURLY")
    private PaymentType paymentType;

    @Valid
    @Schema(description = "새로 추가할 스케줄", example = "[{\"workingDays\": [\"FRIDAY\"], \"startTime\": \"13:00\", \"endTime\": \"21:00\", \"positionsNeeded\": 1, \"position\": \"설거지\"}]")
    private List<CreatePostingScheduleRequestDto> createSchedules;

    @Valid
    @Schema(description = "수정할 스케줄", example = "[{\"id\": 1, \"workingDays\": [\"MONDAY\", \"WEDNESDAY\"], \"startTime\": \"09:00\", \"endTime\": \"18:00\", \"positionsNeeded\": 3, \"position\": \"홀서빙\"}]")
    private List<UpdatePostingScheduleDto> updateSchedules;

    @Schema(description = "삭제할 스케줄 ID", example = "[2, 3]")
    private List<Long> deleteScheduleIds;

    public UpdatePostingCommand toCommand() {
        return new UpdatePostingCommand(
            title,
            description,
            payAmount,
            paymentType,
            toCommands(createSchedules, CreatePostingScheduleRequestDto::toCommand),
            toCommands(updateSchedules, UpdatePostingScheduleDto::toCommand),
            ObjectUtils.isEmpty(deleteScheduleIds) ? List.of() : deleteScheduleIds
        );
    }

    private static <T, R> List<R> toCommands(List<T> source, Function<T, R> mapper) {
        return ObjectUtils.isEmpty(source) ? List.of() : source.stream().map(mapper).toList();
    }
}
