package com.dreamteam.alter.adapter.inbound.common.dto;

import com.dreamteam.alter.domain.common.pagination.CursorPageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CursorPaginatedApiResponse 테스트")
class CursorPaginatedApiResponseTests {

    @Test
    @DisplayName("from_데이터변환과_page매핑")
    void from_데이터변환과_page매핑() {
        // given
        CursorPageResult<Integer> result = CursorPageResult.of("cursor", 10, 3, List.of(1, 2, 3));

        // when
        CursorPaginatedApiResponse<String> mapped = CursorPaginatedApiResponse.from(result, String::valueOf);

        // then
        assertThat(mapped.data()).containsExactly("1", "2", "3");
        assertThat(mapped.page().cursor()).isEqualTo("cursor");
        assertThat(mapped.page().pageSize()).isEqualTo(10);
        assertThat(mapped.page().totalCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("from_빈결과면_빈리스트")
    void from_빈결과면_빈리스트() {
        // given
        CursorPageResult<Integer> result = CursorPageResult.empty(10, 0);

        // when
        CursorPaginatedApiResponse<String> mapped = CursorPaginatedApiResponse.from(result, String::valueOf);

        // then
        assertThat(mapped.data()).isEmpty();
    }
}
