package com.dreamteam.alter.adapter.inbound.manager.posting.dto;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingScheduleRequestDto;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

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

    @NotNull
    @Schema(description = "급여", example = "10000")
    private int payAmount;

    @NotNull
    @Schema(description = "급여 타입", example = "HOURLY")
    private PaymentType paymentType;

    @NotNull
    @Schema(description = "키워드", example = "[2, 3, 1]")
    private List<Long> keywords;

    @Schema(description = "새로 추가할 스케줄", example = "[{\"workingDays\": [\"FRIDAY\"], \"startTime\": \"13:00\", \"endTime\": \"21:00\", \"positionsNeeded\": 1, \"position\": \"설거지\"}]")
    private List<CreatePostingScheduleRequestDto> createSchedules;

    @Schema(description = "수정할 스케줄", example = "[{\"id\": 1, \"workingDays\": [\"MONDAY\", \"WEDNESDAY\"], \"startTime\": \"09:00\", \"endTime\": \"18:00\", \"positionsNeeded\": 3, \"position\": \"홀서빙\"}]")
    private List<UpdatePostingScheduleDto> updateSchedules;

    @Schema(description = "삭제할 스케줄 ID", example = "[2, 3]")
    private List<Long> deleteScheduleIds;
}
