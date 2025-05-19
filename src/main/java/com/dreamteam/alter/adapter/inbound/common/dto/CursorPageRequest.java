package com.dreamteam.alter.adapter.inbound.common.dto;

import org.springdoc.core.annotations.ParameterObject;

@ParameterObject
public record CursorPageRequest<T>(
    T cursor,
    int pageSize
) {
    public static <T> CursorPageRequest<T> of(T cursor, int pageSize) {
        return new CursorPageRequest<>(cursor, pageSize);
    }
}
