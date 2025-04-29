package com.dreamteam.alter.domain.auth.exception;

import org.springframework.security.access.AccessDeniedException;

public class ForbiddenAccessException extends AccessDeniedException {
    public ForbiddenAccessException() {
        super("접근 권한이 없습니다.");
    }
}
