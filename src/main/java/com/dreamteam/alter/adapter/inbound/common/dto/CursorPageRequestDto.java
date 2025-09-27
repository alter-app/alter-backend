package com.dreamteam.alter.adapter.inbound.common.dto;

import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.RequestParam;

@ParameterObject
public record CursorPageRequestDto(
    @Parameter @RequestParam(required = false) String cursor,
    @Parameter(example = "10", required = true) @RequestParam(defaultValue = "10") int pageSize
) {
    private static final int MAX_PAGE_SIZE = 100;

    public CursorPageRequestDto {
        pageSize = Math.min(pageSize, MAX_PAGE_SIZE);
    }

    public static CursorPageRequestDto of(String cursor, int pageSize) {
        return new CursorPageRequestDto(cursor, Math.min(pageSize, MAX_PAGE_SIZE));
    }
}
