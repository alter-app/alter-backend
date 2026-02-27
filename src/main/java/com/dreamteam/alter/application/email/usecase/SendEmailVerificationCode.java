package com.dreamteam.alter.application.email.usecase;

import com.dreamteam.alter.adapter.inbound.general.email.dto.SendEmailVerificationCodeRequestDto;
import com.dreamteam.alter.application.email.event.EmailSendEvent;
import com.dreamteam.alter.application.email.service.VerificationCodeGenerator;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.email.port.inbound.SendEmailVerificationCodeUseCase;
import com.dreamteam.alter.domain.email.entity.EmailSendLog;
import com.dreamteam.alter.domain.email.port.outbound.EmailSendLogRepository;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationSessionStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service("sendEmailVerificationCode")
@RequiredArgsConstructor
@Transactional
public class SendEmailVerificationCode implements SendEmailVerificationCodeUseCase {

    private final EmailVerificationSessionStoreRepository sessionStorePort;
    private final EmailSendLogRepository emailSendLogRepository;
    private final VerificationCodeGenerator codeGenerator;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${alter.email.code-ttl-seconds:300}")
    private long codeTtlSeconds;

    @Value("${alter.email.cooldown-seconds:30}")
    private long cooldownSeconds;


    @Override
    public void execute(SendEmailVerificationCodeRequestDto request) {
        String email = request.getEmail();

        // Check Cooldown
        if (sessionStorePort.isCooldown(email)) {
            throw new CustomException(ErrorCode.TOO_MANY_REQUESTS);
        }

        // Generate Code
        String code = codeGenerator.generate();

        // Save Code (TTL)
        sessionStorePort.saveCode(email, code, Duration.ofSeconds(codeTtlSeconds));

        // Mark Cooldown
        sessionStorePort.markCooldown(email, Duration.ofSeconds(cooldownSeconds));

        // Save to DB for batch Sending (Not Sending immediately)
        EmailSendLog log = EmailSendLog.create(email);

        // Send Email
        EmailSendLog saved = emailSendLogRepository.save(log);

        eventPublisher.publishEvent(new EmailSendEvent(saved.getId(), email, code));
    }
}
