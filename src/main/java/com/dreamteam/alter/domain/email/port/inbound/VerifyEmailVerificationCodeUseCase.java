package com.dreamteam.alter.domain.email.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.amail.dto.VerifyEmailVerificationCodeRequestDto;

public interface VerifyEmailVerificationCodeUseCase {
    void execute(VerifyEmailVerificationCodeRequestDto request);
}
