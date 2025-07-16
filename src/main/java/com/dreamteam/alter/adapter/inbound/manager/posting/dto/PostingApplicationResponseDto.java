package com.dreamteam.alter.adapter.inbound.manager.posting.dto;

import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.ManagerPostingApplicationDetailResponse;
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
@Schema(description = "업장 관리자 공고 지원 상세 조회 응답 DTO")
public class PostingApplicationResponseDto {

    @Schema(description = "공고 지원 ID", example = "1")
    @NotNull
    private Long id;

    @Schema(description = "업장 정보")
    @NotNull
    private PostingApplicationWorkspaceResponseDto workspace;

    @Schema(description = "공고 스케줄 요약 정보")
    @NotNull
    private PostingApplicationResponsePostingScheduleSummaryDto schedule;

    @Schema(description = "지원 내용", example = "안녕하세요, 세븐일레븐에서 일하고 싶습니다.")
    @NotBlank
    private String description;

    @Schema(description = "지원 상태", example = "ACCEPTED")
    @NotNull
    private PostingApplicationStatus status;

    @Schema(description = "지원자 요약 정보")
    @NotNull
    private PostingApplicationResponseApplicantSummaryDto applicant;

    @Schema(description = "지원 일자", example = "2023-10-01T12:00:00")
    @NotNull
    private LocalDateTime createdAt;

    public static PostingApplicationResponseDto from(ManagerPostingApplicationDetailResponse entity) {
        return PostingApplicationResponseDto.builder()
            .id(entity.getId())
            .workspace(PostingApplicationWorkspaceResponseDto.from(entity.getWorkspace()))
            .schedule(PostingApplicationResponsePostingScheduleSummaryDto.from(entity.getSchedule()))
            .description(entity.getDescription())
            .status(entity.getStatus())
            .applicant(PostingApplicationResponseApplicantSummaryDto.from(entity.getUser()))
            .createdAt(entity.getCreatedAt())
            .build();
    }

}
