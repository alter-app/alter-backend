package com.dreamteam.alter.domain.auth.exception;

import org.springframework.security.core.AuthenticationException;

public class RevokedAuthException extends AuthenticationException {
    public RevokedAuthException() {
        super("철회된 인증 정보입니다.");
    }
}
