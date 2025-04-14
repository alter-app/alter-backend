package com.dreamteam.alter.adapter.inbound.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "API 공통 응답 DTO")
public record CommonApiResponse<T>(
    @Schema(description = "응답 시간", example = "2023-10-01T12:00:00")
    String timestamp,

    @Schema(description = "응답 데이터")
    T data
) {

    public static <T> CommonApiResponse<T> empty() {
        return new CommonApiResponse<>(
            String.valueOf(LocalDateTime.now()),
            null
        );
    }

    public static <T> CommonApiResponse<T> of(T data) {
        return new CommonApiResponse<>(
            String.valueOf(LocalDateTime.now()),
            data
        );
    }

}
