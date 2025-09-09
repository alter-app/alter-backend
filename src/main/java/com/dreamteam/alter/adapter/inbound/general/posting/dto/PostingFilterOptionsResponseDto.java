package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "공고 필터링 옵션 응답 DTO")
public class PostingFilterOptionsResponseDto {

    @Schema(description = "시/도 목록")
    private List<String> provinces;

    @Schema(description = "시/군/구 목록")
    private List<String> districts;

    @Schema(description = "읍/면/동 목록")
    private List<String> towns;

    @Schema(description = "정렬 옵션 목록")
    private List<String> sortOptions;

}
