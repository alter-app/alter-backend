package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.PostingDetailWorkspaceResponseDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingDetailResponse;
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
@Schema(description = "공고 상세 조회 응답 DTO")
public class PostingDetailResponseDto {

    @NotNull
    @Schema(description = "공고 ID", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "업장 정보")
    private PostingDetailWorkspaceResponseDto workspace;

    @NotBlank
    @Schema(description = "공고 제목", example = "홀서빙 구합니다")
    private String title;

    @NotBlank
    @Schema(description = "공고 설명", example = "홀서빙 구합니다. 많은 지원 부탁드립니다.")
    private String description;

    @NotNull
    @Schema(description = "급여", example = "10000")
    private int payAmount;

    @NotNull
    @Schema(description = "급여 타입", example = "HOURLY")
    private PaymentType paymentType;

    @NotNull
    @Schema(description = "생성일", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    @NotNull
    @Schema(description = "키워드", example = "[{\"id\":1,\"name\":\"카페\"},{\"id\":4,\"name\":\"분식\"}]")
    private List<PostingKeywordListResponseDto> keywords;

    @NotNull
    @Schema(description = "공고 스케줄", example = "[{\"id\":1,\"workingDays\":[\"MONDAY\",\"WEDNESDAY\"],\"startTime\":\"09:00\",\"endTime\":\"18:00\",\"positionsNeeded\":3,\"positionsAvailable\":2,\"position\":\"홀서빙\"}]")
    private List<PostingScheduleResponseDto> schedules;

    @NotNull
    @Schema(description = "스크랩 여부", example = "true")
    private boolean scrapped;

    public static PostingDetailResponseDto from(PostingDetailResponse entity) {
        return PostingDetailResponseDto.builder()
            .id(entity.getId())
            .workspace(PostingDetailWorkspaceResponseDto.from(entity.getWorkspace()))
            .title(entity.getTitle())
            .description(entity.getDescription())
            .payAmount(entity.getPayAmount())
            .paymentType(entity.getPaymentType())
            .createdAt(entity.getCreatedAt())
            .schedules(entity.getSchedules().stream()
                .map(PostingScheduleResponseDto::from)
                .toList())
            .keywords(entity.getPostingKeywords().stream()
                .map(PostingKeywordListResponseDto::from)
                .toList())
            .scrapped(entity.isScrapped())
            .build();
    }

}
