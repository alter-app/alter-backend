package com.dreamteam.alter.domain.auth.exception;

import org.springframework.security.core.AuthenticationException;

public class InternalAuthException extends AuthenticationException {
    public InternalAuthException() {
        super("내부 인증 오류가 발생했습니다.");
    }
}
