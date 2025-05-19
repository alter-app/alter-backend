package com.dreamteam.alter.adapter.inbound.common.dto;

public record CursorPageResponseDto(
    String cursor,
    int pageSize,
    int totalCount
) {
    public static CursorPageResponseDto of(String cursor, int pageSize, int totalCount) {
        return new CursorPageResponseDto(cursor, pageSize, totalCount);
    }

    public static CursorPageResponseDto empty(int pageSize, int totalCount) {
        return new CursorPageResponseDto(null, pageSize, totalCount);
    }
}
