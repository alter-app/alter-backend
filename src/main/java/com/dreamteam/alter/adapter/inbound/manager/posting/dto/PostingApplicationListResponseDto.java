package com.dreamteam.alter.adapter.inbound.manager.posting.dto;

import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.ManagerPostingApplicationListResponse;
import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "업장 관리자 공고 지원 목록 응답 DTO")
public class PostingApplicationListResponseDto {

    @Schema(description = "공고 지원 ID", example = "1")
    @NotNull
    private Long id;

    @Schema(description = "업장 정보")
    @NotNull
    private PostingApplicationWorkspaceResponseDto workspace;

    @Schema(description = "공고 스케줄 요약 정보")
    @NotNull
    private PostingApplicationResponsePostingScheduleSummaryDto schedule;

    @Schema(description = "지원 상태", example = "ACCEPTED")
    @NotNull
    private PostingApplicationStatus status;

    @Schema(description = "지원 일자", example = "2023-10-01T12:00:00")
    @NotNull
    private LocalDateTime createdAt;

    public static PostingApplicationListResponseDto from(ManagerPostingApplicationListResponse entity) {
        return PostingApplicationListResponseDto.builder()
                .id(entity.getId())
                .workspace(PostingApplicationWorkspaceResponseDto.from(entity.getWorkspace()))
                .schedule(PostingApplicationResponsePostingScheduleSummaryDto.from(entity.getSchedule()))
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

}
