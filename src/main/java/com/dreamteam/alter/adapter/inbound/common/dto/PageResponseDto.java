package com.dreamteam.alter.adapter.inbound.common.dto;

public record PageResponseDto(
    int page,
    int pageSize,
    int totalCount,
    int totalPage
) {
    public static PageResponseDto of(PageRequestDto request, int totalCount) {

        int totalPage = (int) Math.ceil((double) totalCount / request.pageSize());
        totalPage = Math.max(totalPage, 1);
        return new PageResponseDto(request.page(), request.pageSize(), totalCount, totalPage);
    }

    public static PageResponseDto empty(PageRequestDto request) {
        return new PageResponseDto(request.page(), request.pageSize(), 0, 0);
    }
}
