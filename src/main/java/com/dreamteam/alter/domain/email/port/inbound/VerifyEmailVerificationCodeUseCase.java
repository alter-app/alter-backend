package com.dreamteam.alter.domain.email.port.inbound;

import com.dreamteam.alter.domain.email.command.VerifyEmailVerificationCodeCommand;
import com.dreamteam.alter.domain.email.result.VerifyEmailVerificationCodeResult;

public interface VerifyEmailVerificationCodeUseCase {
    VerifyEmailVerificationCodeResult execute(VerifyEmailVerificationCodeCommand command);
}
