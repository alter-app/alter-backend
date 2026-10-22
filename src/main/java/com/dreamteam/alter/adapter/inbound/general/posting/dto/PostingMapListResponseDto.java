package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingListResponse;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "지도 공고 리스트 조회 응답 DTO")
public class PostingMapListResponseDto {

    @NotNull
    @Schema(description = "공고 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "공고 제목", example = "홀서빙 구합니다")
    private String title;

    @NotNull
    @Schema(description = "급여", example = "10000")
    private int payAmount;

    @Schema(description = "공고 모집 인원 (안내용)", example = "2")
    private int recruitCount;

    @NotNull
    @Schema(description = "급여 타입", example = "HOURLY")
    private PaymentType paymentType;

    @NotNull
    @Schema(description = "생성일", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    @NotNull
    @Schema(description = "공고 스케줄", example = "[" +
        "{" +
        "\"workingDays\": [\"MONDAY\", \"WEDNESDAY\"], \"startTime\": \"09:00\", \"endTime\": \"18:00\", \"position\": \"홀서빙\"" +
        "}," +
        "{" +
        "\"workingDays\": [\"FRIDAY\"], \"startTime\": \"13:00\", \"endTime\": \"21:00\", \"position\": \"설거지\"" +
        "}" +
        "]")
    private List<PostingScheduleResponseDto> schedules;

    @NotNull
    @Schema(description = "업장 정보")
    private PostingListWorkspaceResponseDto workspace;

    @NotNull
    @Schema(description = "스크랩 여부", example = "true")
    private boolean scrapped;

    public static PostingMapListResponseDto from(PostingListResponse entity) {
        return PostingMapListResponseDto.builder()
            .id(entity.getId())
            .title(entity.getTitle())
            .payAmount(entity.getPayAmount())
            .recruitCount(entity.getRecruitCount())
            .paymentType(entity.getPaymentType())
            .createdAt(entity.getCreatedAt())
            .schedules(entity.getSchedules().stream()
                .map(PostingScheduleResponseDto::from)
                .toList())
            .workspace(PostingListWorkspaceResponseDto.from(entity.getWorkspace()))
            .scrapped(entity.isScrapped())
            .build();
    }
}
