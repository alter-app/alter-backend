package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.UserPostingApplicationListResponse;
import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "사용자 공고 지원 목록 응답 DTO")
public class UserPostingApplicationListResponseDto {

    @NotNull
    @Schema(description = "지원 ID", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "지원한 공고 스케줄 간략 정보")
    private UserPostingApplicationPostingScheduleSummaryResponseDto postingSchedule;

    @NotNull
    @Schema(description = "지원한 공고 간략 정보")
    private UserPostingApplicationPostingSummaryResponseDto posting;

    @NotBlank
    @Schema(description = "지원내용", example = "잘 부탁드립니다.")
    private String description;

    @NotNull
    @Schema(description = "지원 상태")
    private DescribedEnumDto<PostingApplicationStatus> status;

    @NotNull
    @Schema(description = "생성일시", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    @NotNull
    @Schema(description = "수정일시", example = "2023-10-01T12:00:00")
    private LocalDateTime updatedAt;

    public static UserPostingApplicationListResponseDto from(UserPostingApplicationListResponse entity) {
        return UserPostingApplicationListResponseDto.builder()
            .id(entity.getId())
            .postingSchedule(
                UserPostingApplicationPostingScheduleSummaryResponseDto.from(entity.getPostingSchedule())
            )
            .posting(
                UserPostingApplicationPostingSummaryResponseDto.from(entity.getPosting())
            )
            .description(entity.getDescription())
            .status(DescribedEnumDto.of(entity.getStatus(), PostingApplicationStatus.describe()))
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

}
