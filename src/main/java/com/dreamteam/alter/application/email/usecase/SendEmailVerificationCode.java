package com.dreamteam.alter.application.email.usecase;

import com.dreamteam.alter.adapter.inbound.general.email.dto.SendEmailVerificationCodeRequestDto;
import com.dreamteam.alter.application.email.properties.EmailAuthProperties;
import com.dreamteam.alter.application.email.service.VerificationCodeGenerator;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.email.port.inbound.SendEmailVerificationCodeUseCase;
import com.dreamteam.alter.domain.email.port.outbound.EmailSenderPort;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationTokenStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service("sendEmailVerificationCode")
@RequiredArgsConstructor
@Transactional
public class SendEmailVerificationCode implements SendEmailVerificationCodeUseCase {

    private final EmailVerificationTokenStorePort tokenStorePort;
    private final EmailSenderPort emailSenderPort;
    private final VerificationCodeGenerator codeGenerator;
    private final EmailAuthProperties properties;


    @Override
    public void execute(SendEmailVerificationCodeRequestDto request) {
        String email = request.getEmail();

        // Check Cooldown
        if (tokenStorePort.isCooldown(email)) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_TOO_MANY_REQUESTS);
        }

        // Generate Code
        String code = codeGenerator.generate();

        // Save Code (TTL)
        tokenStorePort.saveCode(email, code, Duration.ofSeconds(properties.getCodeTtlSeconds()));

        // Mark Cooldown
        tokenStorePort.markCooldown(email,Duration.ofSeconds(properties.getCooldownSeconds()));

        // Send Email
        emailSenderPort.sendVerificationCode(email, code);
    }
}
