package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.CoordinateDto;
import com.dreamteam.alter.domain.posting.type.PostingSortType;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springdoc.core.annotations.ParameterObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "지도 공고 목록 조회 필터 DTO")
public class PostingMapListFilterDto {

    @Parameter(description = "지도 좌측 상단 좌표")
    private CoordinateDto coordinate1;

    @Parameter(description = "지도 우측 하단 좌표")
    private CoordinateDto coordinate2;

    @Parameter(description = "검색 키워드 (공고 제목 또는 업장명)")
    private String searchKeyword;

    @NotNull
    @Parameter(description = "정렬 기준")
    private PostingSortType sortType;
}
