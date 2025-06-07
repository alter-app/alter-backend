package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.UserFavoritePostingListResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "사용자 스크랩한 공고 목록 응답 DTO")
public class UserFavoritePostingListResponseDto {

    @NotNull
    @Schema(description = "스크랩 ID", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "스크랩한 공고 정보")
    private UserFavoritePostingListPostingSummaryResponseDto posting;

    @NotNull
    @Schema(description = "스크랩한 날짜", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    public static UserFavoritePostingListResponseDto from(UserFavoritePostingListResponse entity) {
        return com.dreamteam.alter.adapter.inbound.general.user.dto.UserFavoritePostingListResponseDto.builder()
            .id(entity.getId())
            .posting(UserFavoritePostingListPostingSummaryResponseDto.from(entity.getPosting()))
            .createdAt(entity.getCreatedAt())
            .build();
    }

}
