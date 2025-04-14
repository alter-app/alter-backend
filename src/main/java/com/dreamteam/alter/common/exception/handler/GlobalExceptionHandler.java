package com.dreamteam.alter.common.exception.handler;

import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.exception.SignupRequiredException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorCode errorCode = ErrorCode.ILLEGAL_ARGUMENT;
        return ResponseEntity.status(errorCode.getStatus())
            .body(ErrorResponse.of(errorCode));
    }

    @ExceptionHandler(SignupRequiredException.class)
    public ResponseEntity<ErrorResponse<String>> handleSignupRequiredException(SignupRequiredException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus())
            .body(ErrorResponse.of(e.getErrorCode(), e.getSignupSessionId()));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse<Void>> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus())
            .body(ErrorResponse.of(e.getErrorCode()));
    }

}
