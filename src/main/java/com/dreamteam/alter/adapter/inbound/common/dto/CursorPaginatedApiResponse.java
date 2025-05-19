package com.dreamteam.alter.adapter.inbound.common.dto;

import java.util.Collections;
import java.util.List;

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
}
