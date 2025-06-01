package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.domain.posting.entity.PostingKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(title = "키워드 리스트 응답 DTO")
public class PostingKeywordListResponseDto {

    @NotNull
    @Schema(title = "키워드 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(title = "키워드 이름", example = "홀서빙")
    private String name;

    public static PostingKeywordListResponseDto from(PostingKeyword postingKeyword) {
        return new PostingKeywordListResponseDto(
            postingKeyword.getId(),
            postingKeyword.getName()
        );
    }

}
