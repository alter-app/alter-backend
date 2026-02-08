package com.dreamteam.alter.domain.email.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.email.dto.VerifyEmailVerificationCodeRequestDto;

public interface VerifyEmailVerificationCodeUseCase {
    void execute(VerifyEmailVerificationCodeRequestDto request);
}
