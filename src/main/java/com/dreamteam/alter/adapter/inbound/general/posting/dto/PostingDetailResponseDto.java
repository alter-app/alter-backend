package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingDetailResponse;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "공고 상세 조회 응답 DTO")
public class PostingDetailResponseDto {

    @Schema(description = "공고 ID", example = "1")
    private Long id;

    @Column(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Schema(description = "공고 제목", example = "홀서빙 구합니다")
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Schema(description = "급여", example = "10000")
    private int payAmount;

    @Schema(description = "급여 타입", example = "HOURLY")
    private PaymentType paymentType;

    @Schema(description = "생성일", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "키워드", example = "[{\"id\":1,\"name\":\"카페\"},{\"id\":4,\"name\":\"분식\"}]")
    private List<KeywordListResponseDto> keywords;

    @Schema(description = "공고 스케줄", example = "[" +
        "{" +
        "\"workingDays\": [\"MONDAY\", \"WEDNESDAY\"], \"startTime\": \"09:00\", \"endTime\": \"18:00\", \"positionsNeeded\": 3, \"position\": 3" +
        "}," +
        "{" +
        "\"workingDays\": [\"FRIDAY\"], \"startTime\": \"13:00\", \"endTime\": \"21:00\", \"positionsNeeded\": 1, \"position\": 2" +
        "}" +
        "]")
    private List<PostingScheduleResponseDto> schedules;

    public static PostingDetailResponseDto from(PostingDetailResponse entity) {
        return PostingDetailResponseDto.builder()
            .id(entity.getId())
            .workspace(entity.getWorkspace())
            .title(entity.getTitle())
            .description(entity.getDescription())
            .payAmount(entity.getPayAmount())
            .paymentType(entity.getPaymentType())
            .createdAt(entity.getCreatedAt())
            .schedules(entity.getSchedules().stream()
                .map(PostingScheduleResponseDto::from)
                .toList())
            .keywords(entity.getPostingKeywords().stream()
                .map(KeywordListResponseDto::from)
                .toList())
            .build();
    }

}
