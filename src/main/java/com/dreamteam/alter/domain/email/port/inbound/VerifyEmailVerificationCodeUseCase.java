package com.dreamteam.alter.domain.email.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.email.dto.VerifyEmailVerificationCodeRequestDto;
import com.dreamteam.alter.adapter.inbound.general.email.dto.VerifyEmailVerificationCodeResponseDto;

public interface VerifyEmailVerificationCodeUseCase {
    VerifyEmailVerificationCodeResponseDto execute(VerifyEmailVerificationCodeRequestDto request);
}
