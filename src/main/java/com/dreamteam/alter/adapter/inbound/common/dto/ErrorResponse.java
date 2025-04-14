package com.dreamteam.alter.adapter.inbound.common.dto;

import com.dreamteam.alter.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "API 에러 응답 DTO")
public record ErrorResponse<T>(
    @Schema(description = "응답 시간", example = "2023-10-01T12:00:00")
    String timestamp,

    @Schema(description = "에러 코드", example = "A001")
    String code,

    @Schema(description = "에러 메시지", example = "에러가 발생했습니다.")
    String message,

    @Schema(description = "에러 응답 데이터")
    T data
) {

    public static <T> ErrorResponse<T> of(ErrorCode errorCode) {
        return new ErrorResponse<>(
            String.valueOf(LocalDateTime.now()),
            errorCode.getCode(),
            errorCode.getMessage(),
            null
        );
    }

    public static <T> ErrorResponse<T> of(ErrorCode errorCode, T data) {
        return new ErrorResponse<>(
            String.valueOf(LocalDateTime.now()),
            errorCode.getCode(),
            errorCode.getMessage(),
            data
        );
    }

}
