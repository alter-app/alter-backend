package com.dreamteam.alter.domain.auth.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidAuthException extends AuthenticationException {
    public InvalidAuthException() {
        super("유효하지 않은 인증 정보입니다.");
    }
}
