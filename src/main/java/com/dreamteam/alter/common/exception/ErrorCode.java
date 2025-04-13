package com.dreamteam.alter.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    UNAUTHORIZED(401, "A001", "인증되지 않은 사용자입니다."),
    FORBIDDEN(403, "A002", "접근 권한이 없습니다."),

    ILLEGAL_ARGUMENT(400, "B001", "잘못된 요청입니다."),
    ;

    private final int status;
    private final String code;
    private final String message;

}
