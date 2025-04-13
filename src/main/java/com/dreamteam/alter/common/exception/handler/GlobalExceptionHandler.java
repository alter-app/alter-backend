package com.dreamteam.alter.common.exception.handler;

import com.dreamteam.alter.common.dto.ErrorResponse;
import com.dreamteam.alter.common.exception.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorCode errorCode = ErrorCode.ILLEGAL_ARGUMENT;
        return ResponseEntity.status(errorCode.getStatus())
            .body(ErrorResponse.of(errorCode, e.getMessage()));
    }

}
