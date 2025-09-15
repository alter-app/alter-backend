package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.domain.posting.type.PostingSortType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "공고 필터링 옵션 응답 DTO")
public class PostingFilterOptionsResponseDto {

    @Schema(description = "시/도 목록")
    private List<String> provinces;

    @Schema(description = "시/군/구 목록")
    private List<String> districts;

    @Schema(description = "읍/면/동 목록")
    private List<String> towns;

    @Schema(description = "정렬 옵션 목록")
    private List<DescribedEnumDto<PostingSortType>> sortOptions;

    public static PostingFilterOptionsResponseDto of(
        List<String> provinces,
        List<String> districts,
        List<String> towns,
        List<PostingSortType> sortOptions
    ) {
        return PostingFilterOptionsResponseDto.builder()
            .provinces(provinces)
            .districts(districts)
            .towns(towns)
            .sortOptions(sortOptions.stream()
                .map(sortType -> DescribedEnumDto.of(sortType, PostingSortType.describe()))
                .toList()
            )
            .build();
    }

}
