package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.domain.posting.entity.Keyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(title = "키워드 리스트 응답 DTO")
public class KeywordListResponseDto {

    @Schema(title = "키워드 ID", example = "1")
    private Long id;

    @Schema(title = "키워드 이름", example = "홀서빙")
    private String name;

    public static KeywordListResponseDto from(Keyword keyword) {
        return new KeywordListResponseDto(
            keyword.getId(),
            keyword.getName()
        );
    }

}
