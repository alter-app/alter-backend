package com.dreamteam.alter.common.exception.handler;

import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.SignupSessionResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.exception.FieldErrorDetail;
import com.dreamteam.alter.domain.auth.exception.SignupRequiredException;
import com.dreamteam.alter.domain.workspace.exception.InvitationUnavailableException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

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

    @ExceptionHandler(InvitationUnavailableException.class)
    public ResponseEntity<ErrorResponse<List<String>>> handleInvitationUnavailableException(InvitationUnavailableException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus())
            .body(ErrorResponse.of(e.getErrorCode(), e.getMessage(), e.getUnavailablePhoneNumbers()));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse<Void>> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus())
            .body(ErrorResponse.of(e.getErrorCode(), e.getMessage()));
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        ErrorCode errorCode = ErrorCode.ILLEGAL_ARGUMENT;
        return ResponseEntity.status(errorCode.getStatus())
            .body(ErrorResponse.of(errorCode));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse<Void>> handleMissingServletRequestPartException(MissingServletRequestPartException e) {
        ErrorCode errorCode = ErrorCode.ILLEGAL_ARGUMENT;
        return ResponseEntity.status(errorCode.getStatus())
            .body(ErrorResponse.of(errorCode, "Multipart가 누락됐습니다."));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse<Void>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        ErrorCode errorCode = ErrorCode.ILLEGAL_ARGUMENT;
        return ResponseEntity.status(errorCode.getStatus())
            .body(ErrorResponse.of(errorCode, "파라미터가 누락됐습니다."));
    }

}
