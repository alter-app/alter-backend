package com.dreamteam.alter.domain.auth.exception;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SignupSessionResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class SignupRequiredException extends CustomException {

    private final SignupSessionResponseDto signupSessionResponseDto;

    public SignupRequiredException(SignupSessionResponseDto signupSessionResponseDto) {
        super(ErrorCode.SIGNUP_REQUIRED);
        this.signupSessionResponseDto = signupSessionResponseDto;
    }

}
