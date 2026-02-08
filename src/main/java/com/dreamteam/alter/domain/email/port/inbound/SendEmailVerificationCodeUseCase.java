package com.dreamteam.alter.domain.email.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.amail.dto.SendEmailVerificationCodeRequestDto;

public interface SendEmailVerificationCodeUseCase {
    void execute(SendEmailVerificationCodeRequestDto request);
}
