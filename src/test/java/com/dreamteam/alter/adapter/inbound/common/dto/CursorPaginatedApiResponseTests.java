package com.dreamteam.alter.adapter.inbound.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CursorPaginatedApiResponse 테스트")
class CursorPaginatedApiResponseTests {

    @Test
    @DisplayName("map_데이터만_변환하고_page는_유지")
    void map_데이터만_변환하고_page는_유지() {
        // given
        CursorPageResponseDto page = CursorPageResponseDto.of("cursor", 10, 3);
        CursorPaginatedApiResponse<Integer> response = CursorPaginatedApiResponse.of(page, List.of(1, 2, 3));

        // when
        CursorPaginatedApiResponse<String> mapped = response.map(String::valueOf);

        // then
        assertThat(mapped.data()).containsExactly("1", "2", "3");
        assertThat(mapped.page()).isSameAs(page);
    }

    @Test
    @DisplayName("map_빈데이터면_빈리스트")
    void map_빈데이터면_빈리스트() {
        // given
        CursorPageResponseDto page = CursorPageResponseDto.empty(10, 0);
        CursorPaginatedApiResponse<Integer> response = CursorPaginatedApiResponse.empty(page);

        // when
        CursorPaginatedApiResponse<String> mapped = response.map(String::valueOf);

        // then
        assertThat(mapped.data()).isEmpty();
    }
}
