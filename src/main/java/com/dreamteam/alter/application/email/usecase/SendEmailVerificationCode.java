package com.dreamteam.alter.application.email.usecase;

import com.dreamteam.alter.adapter.inbound.general.email.dto.SendEmailVerificationCodeRequestDto;
import com.dreamteam.alter.application.email.event.EmailSendEvent;
import com.dreamteam.alter.application.email.properties.EmailAuthProperties;
import com.dreamteam.alter.application.email.service.VerificationCodeGenerator;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.email.port.inbound.SendEmailVerificationCodeUseCase;
import com.dreamteam.alter.domain.email.entity.EmailSendLog;
import com.dreamteam.alter.domain.email.type.EmailSendStatus;
import com.dreamteam.alter.domain.email.port.outbound.EmailSendLogPort;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationTokenStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service("sendEmailVerificationCode")
@RequiredArgsConstructor
@Transactional
public class SendEmailVerificationCode implements SendEmailVerificationCodeUseCase {

    private final EmailVerificationTokenStorePort tokenStorePort;
    private final EmailSendLogPort emailSendLogPort;
    private final VerificationCodeGenerator codeGenerator;
    private final EmailAuthProperties properties;
    private final ApplicationEventPublisher eventPublisher;


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
        tokenStorePort.markCooldown(email, Duration.ofSeconds(properties.getCooldownSeconds()));

        // Save to DB for batch Sending (Not Sending immediately)
        EmailSendLog log = EmailSendLog.builder()
                .email(email)
                .code(code)
                .status(EmailSendStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        // Send Email
        EmailSendLog saved = emailSendLogPort.save(log);

        eventPublisher.publishEvent(new EmailSendEvent(saved.getId()));
    }
}
