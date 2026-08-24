package com.dreamteam.alter.adapter.inbound.common.dto;

import com.dreamteam.alter.domain.common.pagination.CursorPageResult;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public record CursorPaginatedApiResponse<T>(
    CursorPageResponseDto page,
    List<T> data
) {
    public static <T> CursorPaginatedApiResponse<T> of(CursorPageResponseDto page, List<T> data) {
        return new CursorPaginatedApiResponse<>(page, data);
    }

    public static <T> CursorPaginatedApiResponse<T> empty(CursorPageResponseDto page) {
        return new CursorPaginatedApiResponse<>(page, Collections.emptyList());
    }

    public static <T, R> CursorPaginatedApiResponse<R> from(CursorPageResult<T> result, Function<T, R> mapper) {
        return new CursorPaginatedApiResponse<>(
            CursorPageResponseDto.of(result.nextCursor(), result.pageSize(), result.totalCount()),
            result.data().stream().map(mapper).toList()
        );
    }
}
