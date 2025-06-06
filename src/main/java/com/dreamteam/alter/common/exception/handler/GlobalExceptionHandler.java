package com.dreamteam.alter.common.exception.handler;

import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.SignupSessionResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.exception.FieldErrorDetail;
import com.dreamteam.alter.domain.auth.exception.SignupRequiredException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorCode errorCode = ErrorCode.ILLEGAL_ARGUMENT;
        return ResponseEntity.status(errorCode.getStatus())
            .body(ErrorResponse.of(errorCode));
    }

    @ExceptionHandler(SignupRequiredException.class)
    public ResponseEntity<ErrorResponse<SignupSessionResponseDto>> handleSignupRequiredException(SignupRequiredException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus())
            .body(ErrorResponse.of(e.getErrorCode(), e.getSignupSessionResponseDto()));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse<Void>> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus())
            .body(ErrorResponse.of(e.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<List<FieldErrorDetail>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldErrorDetail> details = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new FieldErrorDetail(
                error.getField(),
                error.getDefaultMessage()
            ))
            .toList();

        ErrorCode errorCode = ErrorCode.ILLEGAL_ARGUMENT;
        return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.of(errorCode, details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse<List<FieldErrorDetail>>> handleConstraintViolationException(ConstraintViolationException e) {
        List<FieldErrorDetail> details = e.getConstraintViolations()
            .stream()
            .map(violation -> new FieldErrorDetail(
                violation.getPropertyPath().toString(),
                violation.getMessage()
            ))
            .toList();

        ErrorCode errorCode = ErrorCode.ILLEGAL_ARGUMENT;
        return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.of(errorCode, details));
    }

}
