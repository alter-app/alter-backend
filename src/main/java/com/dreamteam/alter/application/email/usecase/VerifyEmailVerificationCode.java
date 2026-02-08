package com.dreamteam.alter.application.email.usecase;

import com.dreamteam.alter.adapter.inbound.general.email.dto.VerifyEmailVerificationCodeRequestDto;
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
            // 시도 횟수 증가
            long attempts = tokenStorePort.incrementAttempt(email, Duration.ofSeconds(properties.getCodeTtlSeconds()));

            if (attempts >= properties.getMaxAttempts()) {
                tokenStorePort.deleteCode(email);
                throw new CustomException(ErrorCode.EMAIL_VERIFICATION_EXCEEDED_MAX_ATTEMPTS);
            }

            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        // Success -> Delete Code & Mark Verified
        tokenStorePort.deleteCode(email);
        tokenStorePort.markVerified(email, Duration.ofSeconds(properties.getVerifiedTtlSeconds()));
    }
}
