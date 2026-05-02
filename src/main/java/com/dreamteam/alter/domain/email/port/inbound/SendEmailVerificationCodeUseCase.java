package com.dreamteam.alter.domain.email.port.inbound;

import com.dreamteam.alter.domain.email.command.SendEmailVerificationCodeCommand;

public interface SendEmailVerificationCodeUseCase {
    void execute(SendEmailVerificationCodeCommand command);
}
