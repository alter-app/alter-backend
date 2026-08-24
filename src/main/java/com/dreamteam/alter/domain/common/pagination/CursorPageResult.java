package com.dreamteam.alter.domain.common.pagination;

import java.util.Collections;
import java.util.List;

public record CursorPageResult<T>(
    String nextCursor,
    int pageSize,
    int totalCount,
    List<T> data
) {
    public static <T> CursorPageResult<T> of(String nextCursor, int pageSize, int totalCount, List<T> data) {
        return new CursorPageResult<>(nextCursor, pageSize, totalCount, data);
    }

    public static <T> CursorPageResult<T> empty(int pageSize, int totalCount) {
        return new CursorPageResult<>(null, pageSize, totalCount, Collections.emptyList());
    }
}
