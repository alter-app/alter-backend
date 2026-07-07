package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.domain.posting.type.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "공고 생성 요청 DTO")
public class CreatePostingRequestDto {

    @Schema(description = "업장 ID (임시)", example = "1")
    @NotNull
    private Long workspaceId;

    @Schema(description = "공고 제목", example = "홀서빙 구합니다")
    @NotBlank
    private String title;

    @Schema(description = "공고 설명", example = "홀서빙 구합니다. 주말 근무 가능하신 분 우대합니다.")
    @NotBlank
    private String description;

    @Schema(description = "급여", example = "10000")
    @Positive
    private int payAmount;

    @Schema(description = "급여 타입", example = "HOURLY")
    @NotNull
    private PaymentType paymentType;

    @Schema(description = "마스터 업종 ID 목록 (선택)", example = "[2, 3, 1]")
    private List<Long> keywords;

    @Schema(description = "직접입력 업종 (마스터 미등록, 공고 라벨로 저장)", example = "[\"브런치카페\"]")
    @Size(max = 10)
    private List<@NotBlank @Size(max = 128) String> customKeywords;

    @Schema(description = "공고 스케줄", example = "[" +
            "{" +
                "\"workingDays\": [\"MONDAY\", \"WEDNESDAY\"], \"startTime\": \"09:00\", \"endTime\": \"18:00\", \"positionsNeeded\": 3, \"position\": \"설거지\"" +
            "}," +
            "{" +
                "\"workingDays\": [\"FRIDAY\"], \"startTime\": \"13:00\", \"endTime\": \"21:00\", \"positionsNeeded\": 1, \"position\": \"홀서빙\"" +
            "}" +
        "]")
    @Valid
    private List<CreatePostingScheduleRequestDto> schedules;

    @Schema(hidden = true)
    @AssertTrue(message = "업종은 최소 1개 이상 선택하거나 직접 입력해야 합니다.")
    public boolean isKeywordProvided() {
        return ObjectUtils.isNotEmpty(keywords) || ObjectUtils.isNotEmpty(customKeywords);
    }

}
