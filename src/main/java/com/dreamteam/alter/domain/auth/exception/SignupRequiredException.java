package com.dreamteam.alter.domain.auth.exception;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class SignupRequiredException extends CustomException {

    private final String signupSessionId;

    public SignupRequiredException(String signupSessionId) {
        super(ErrorCode.SIGNUP_REQUIRED);
        this.signupSessionId = signupSessionId;
    }

}
