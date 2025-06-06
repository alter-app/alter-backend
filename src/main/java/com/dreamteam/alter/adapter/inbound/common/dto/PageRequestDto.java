package com.dreamteam.alter.adapter.inbound.common.dto;

import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.RequestParam;

@ParameterObject
public record PageRequestDto(
    @Parameter(required = true, example = "1") @RequestParam(defaultValue = "1") int page,
    @Parameter(required = true, example = "10") @RequestParam(defaultValue = "10") int pageSize
) {
    private static final int MAX_PAGE_SIZE = 100;

    public static PageRequestDto of(int page, int pageSize) {
        return new PageRequestDto(page, Math.min(pageSize, MAX_PAGE_SIZE));
    }

    public int getOffset() {
        return (page - 1) * pageSize;
    }

    public int getLimit() {
        return pageSize;
    }

}
