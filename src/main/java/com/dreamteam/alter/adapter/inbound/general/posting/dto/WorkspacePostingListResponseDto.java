package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "업장 공고 목록 응답 DTO")
public class WorkspacePostingListResponseDto {

    @NotNull
    @Schema(description = "업장 정보")
    private WorkspaceInfo workspace;

    @NotNull
    @Schema(description = "공고 목록")
    private List<PostingInfo> postings;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    @Schema(description = "업장 정보")
    public static class WorkspaceInfo {

        @NotNull
        @Schema(description = "업장 ID", example = "1")
        private Long id;

        @NotNull
        @Schema(description = "업장 이름", example = "카페 알터")
        private String businessName;

        @NotNull
        @Schema(description = "위도", example = "37.5665")
        private BigDecimal latitude;

        @NotNull
        @Schema(description = "경도", example = "126.9780")
        private BigDecimal longitude;

        public static WorkspaceInfo of(Long id, String businessName, BigDecimal latitude, BigDecimal longitude) {
            return WorkspaceInfo.builder()
                .id(id)
                .businessName(businessName)
                .latitude(latitude)
                .longitude(longitude)
                .build();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    @Schema(description = "공고 정보")
    public static class PostingInfo {

        @NotNull
        @Schema(description = "공고 ID", example = "1")
        private Long id;

        @NotNull
        @Schema(description = "제목", example = "카페 알바 구합니다")
        private String title;

        @NotNull
        @Schema(description = "급여", example = "10000")
        private Integer payAmount;

        @NotNull
        @Schema(description = "생성일시", example = "2024-01-01T10:00:00")
        private LocalDateTime createdAt;

        public static PostingInfo of(Long id, String title, Integer payAmount, LocalDateTime createdAt) {
            return PostingInfo.builder()
                .id(id)
                .title(title)
                .payAmount(payAmount)
                .createdAt(createdAt)
                .build();
        }
    }

    public static WorkspacePostingListResponseDto of(WorkspaceInfo workspace, List<PostingInfo> postings) {
        return WorkspacePostingListResponseDto.builder()
            .workspace(workspace)
            .postings(postings)
            .build();
    }
}


