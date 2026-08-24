package com.dreamteam.alter.domain.common.pagination;

public record CursorPageQuery(
    String cursor,
    int pageSize
) {
    public static CursorPageQuery of(String cursor, int pageSize) {
        return new CursorPageQuery(cursor, pageSize);
    }
}
