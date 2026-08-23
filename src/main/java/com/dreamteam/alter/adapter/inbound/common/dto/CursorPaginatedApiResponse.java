package com.dreamteam.alter.adapter.inbound.common.dto;

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

    public <R> CursorPaginatedApiResponse<R> map(Function<T, R> mapper) {
        return CursorPaginatedApiResponse.of(page(), data().stream().map(mapper).toList());
    }
}
