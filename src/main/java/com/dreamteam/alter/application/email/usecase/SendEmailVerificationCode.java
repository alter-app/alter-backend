package com.dreamteam.alter.application.email.usecase;

import com.dreamteam.alter.adapter.inbound.general.amail.dto.SendEmailVerificationCodeRequestDto;
import com.dreamteam.alter.application.email.properties.EmailAuthProperties;
import com.dreamteam.alter.application.email.service.VerificationCodeGenerator;
import com.dreamteam.alter.domain.email.port.inbound.SendEmailVerificationCodeUseCase;
import com.dreamteam.alter.domain.email.port.outbound.EmailSenderPort;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationTokenStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    }
}
