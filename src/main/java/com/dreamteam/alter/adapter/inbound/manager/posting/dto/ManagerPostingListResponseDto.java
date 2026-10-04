package com.dreamteam.alter.adapter.inbound.manager.posting.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.ManagerPostingListWorkspaceResponseDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingScheduleResponseDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.ManagerPostingListResponse;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
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
@Schema(description = "자신이 등록한 공고 리스트 조회 응답 DTO")
public class ManagerPostingListResponseDto {

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

    @Schema(description = "합격자 수 (ACCEPTED 지원서 수, 모집 인원 초과 가능)", example = "3")
    private long acceptedCount;

    @NotNull
    @Schema(description = "급여 타입", example = "HOURLY")
    private PaymentType paymentType;

    @NotNull
    @Schema(description = "공고 상태")
    private DescribedEnumDto<PostingStatus> status;

    @NotNull
    @Schema(description = "생성일", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    @NotNull
    @Schema(description = "공고 스케줄", example = "[" +
        "{" +
        "\"workingDays\": [\"MONDAY\", \"WEDNESDAY\"], \"startTime\": \"09:00\", \"endTime\": \"18:00\", \"position\": 3" +
        "}," +
        "{" +
        "\"workingDays\": [\"FRIDAY\"], \"startTime\": \"13:00\", \"endTime\": \"21:00\", \"position\": 2" +
        "}" +
        "]")
    private List<PostingScheduleResponseDto> schedules;

    @NotNull
    @Schema(description = "업장 정보")
    private ManagerPostingListWorkspaceResponseDto workspace;

    public static ManagerPostingListResponseDto of(ManagerPostingListResponse response, long acceptedCount) {
        return ManagerPostingListResponseDto.builder()
            .id(response.getId())
            .title(response.getTitle())
            .payAmount(response.getPayAmount())
            .recruitCount(response.getRecruitCount())
            .acceptedCount(acceptedCount)
            .paymentType(response.getPaymentType())
            .status(DescribedEnumDto.of(response.getStatus(), PostingStatus.describe()))
            .createdAt(response.getCreatedAt())
            .schedules(response.getSchedules().stream()
                .map(PostingScheduleResponseDto::from)
                .toList())
            .workspace(ManagerPostingListWorkspaceResponseDto.from(response.getWorkspace()))
            .build();
    }

}
