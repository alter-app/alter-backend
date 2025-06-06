package com.dreamteam.alter.adapter.inbound.common.dto;

import java.util.Collections;
import java.util.List;

public record PaginatedResponseDto<T>(
    PageResponseDto page,
    List<T> data
) {
    public static <T> PaginatedResponseDto<T> of(PageResponseDto page, List<T> data) {

        return new PaginatedResponseDto<>(page, data);
    }

    public static <T> PaginatedResponseDto<T> empty(PageResponseDto page) {

        return new PaginatedResponseDto<>(page, Collections.emptyList());
    }
}
