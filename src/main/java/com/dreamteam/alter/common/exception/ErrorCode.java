package com.dreamteam.alter.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    UNAUTHORIZED(401, "A001", "인증되지 않은 사용자입니다."),
    FORBIDDEN(403, "A002", "접근 권한이 없습니다."),
    SIGNUP_REQUIRED(400, "A003", "회원가입이 필요합니다."),
    EMAIL_DUPLICATED(400, "A004", "이미 가입된 이메일입니다."),
    SOCIAL_ID_DUPLICATED(400, "A005", "이미 가입된 소셜 계정입니다."),
    SIGNUP_SESSION_NOT_EXIST(400, "A006", "회원 가입 세션이 존재하지 않습니다."),
    SOCIAL_TOKEN_EXPIRED(400, "A007", "소셜 토큰이 만료되었습니다."),
    NICKNAME_DUPLICATED(400, "A008", "이미 사용중인 닉네임입니다."),

    ILLEGAL_ARGUMENT(400, "B001", "잘못된 요청입니다."),
    REFRESH_TOKEN_REQUIRED(400, "B002", "RefreshToken을 통해 요청해야 합니다."),
    SUSPENDED_USER(400, "B003", "이용이 정지된 사용자입니다."),
    DELETED_USER(400, "B004", "탈퇴한 사용자입니다."),
    INVALID_CURSOR(400, "B005", "잘못된 커서 페이징 요청입니다."),
    INVALID_KEYWORD(400, "B006", "잘못된 키워드 요청입니다."),
    POSTING_NOT_FOUND(400, "B007", "존재하지 않는 공고입니다."),

    INTERNAL_SERVER_ERROR(400, "C001", "서버 내부 오류입니다."),
    ;

    private final int status;
    private final String code;
    private final String message;

}
