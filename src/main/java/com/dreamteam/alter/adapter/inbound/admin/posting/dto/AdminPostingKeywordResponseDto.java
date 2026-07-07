package com.dreamteam.alter.adapter.inbound.admin.posting.dto;

import com.dreamteam.alter.domain.posting.entity.PostingKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "업종(키워드) 응답 DTO")
public class AdminPostingKeywordResponseDto {

    @Schema(description = "업종 ID", example = "1")
    private Long id;

    @Schema(description = "업종명", example = "카페")
    private String name;

    @Schema(description = "업종 설명", example = "카페/디저트 매장")
    private String description;

    public static AdminPostingKeywordResponseDto from(PostingKeyword keyword) {
        return AdminPostingKeywordResponseDto.builder()
            .id(keyword.getId())
            .name(keyword.getName())
            .description(keyword.getDescription())
            .build();
    }

    public static AdminPostingKeywordResponseDto of(Long id, String name, String description) {
        return AdminPostingKeywordResponseDto.builder()
            .id(id)
            .name(name)
            .description(description)
            .build();
    }
}
