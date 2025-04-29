package com.dreamteam.alter.domain.auth.exception;

import org.springframework.security.core.AuthenticationException;

public class ExpiredAuthException extends AuthenticationException {
    public ExpiredAuthException() {
        super("만료된 인증 정보입니다.");
    }
}
