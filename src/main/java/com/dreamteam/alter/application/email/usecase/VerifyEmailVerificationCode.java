package com.dreamteam.alter.application.email.usecase;

import com.dreamteam.alter.adapter.inbound.general.amail.dto.VerifyEmailVerificationCodeRequestDto;
import com.dreamteam.alter.application.email.properties.EmailAuthProperties;
import com.dreamteam.alter.domain.email.port.inbound.VerifyEmailVerificationCodeUseCase;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationTokenStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("verifyEmailVerificationCode")
@RequiredArgsConstructor
@Transactional
public class VerifyEmailVerificationCode implements VerifyEmailVerificationCodeUseCase {

    private final EmailVerificationTokenStorePort tokenStorePort;
    private final EmailAuthProperties properties;

    @Override
    public void execute(VerifyEmailVerificationCodeRequestDto request) {

    }
}
