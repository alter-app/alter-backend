package com.dreamteam.alter.application.email.usecase;

import com.dreamteam.alter.adapter.inbound.general.amail.dto.VerifyEmailVerificationCodeRequestDto;
import com.dreamteam.alter.application.email.properties.EmailAuthProperties;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.email.port.inbound.VerifyEmailVerificationCodeUseCase;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationTokenStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service("verifyEmailVerificationCode")
@RequiredArgsConstructor
@Transactional
public class VerifyEmailVerificationCode implements VerifyEmailVerificationCodeUseCase {

    private final EmailVerificationTokenStorePort tokenStorePort;
    private final EmailAuthProperties properties;

    @Override
    public void execute(VerifyEmailVerificationCodeRequestDto request) {
        String email = request.getEmail();
        String inputCode = request.getCode();

        // Find Code
        String storedCode = tokenStorePort.findCode(email)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_CODE_EXPIRED));

        // Compare
        if (!storedCode.equals(inputCode)) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        // Success -> Delete Code & Mark Verified
        tokenStorePort.deleteCode(email);
        tokenStorePort.markVerified(email, Duration.ofSeconds(properties.getVerifiedTtlSeconds()));
    }
}
