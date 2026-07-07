package com.dreamteam.alter.adapter.inbound.admin.posting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업종(키워드) 생성/수정 요청 DTO")
public class AdminPostingKeywordRequestDto {

    @NotBlank
    @Size(max = 128)
    @Schema(description = "업종명", example = "카페")
    private String name;

    @Size(max = 255)
    @Schema(description = "업종 설명", example = "카페/디저트 매장")
    private String description;
}
