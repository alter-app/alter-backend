package com.dreamteam.alter.application.email.usecase;

import com.dreamteam.alter.adapter.inbound.general.email.dto.VerifyEmailVerificationCodeRequestDto;
import com.dreamteam.alter.adapter.inbound.general.email.dto.VerifyEmailVerificationCodeResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.email.port.inbound.VerifyEmailVerificationCodeUseCase;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationSessionStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service("verifyEmailVerificationCode")
@RequiredArgsConstructor
@Transactional
public class VerifyEmailVerificationCode implements VerifyEmailVerificationCodeUseCase {

    private final EmailVerificationSessionStoreRepository sessionStoreRepository;

    @Value("${alter.email.code-ttl-seconds:300}")
    private long codeTtlSeconds;

    @Value("${alter.email.verified-ttl-seconds:900}")
    private long verifiedTtlSeconds;

    @Value("${alter.email.max-attempts:5}")
    private int maxAttempts;

    @Override
    public VerifyEmailVerificationCodeResponseDto execute(VerifyEmailVerificationCodeRequestDto request) {
        String email = request.getEmail();
        String inputCode = request.getCode();

        // Find Code
        String storedCode = sessionStoreRepository.findCode(email)
                .orElseThrow(() -> new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "인증 코드가 없거나 만료되었습니다."));

        // Compare
        if (!storedCode.equals(inputCode)) {
            // 시도 횟수 증가
            long attempts = sessionStoreRepository.incrementAttempt(email, Duration.ofSeconds(codeTtlSeconds));

            if (attempts >= maxAttempts) {
                sessionStoreRepository.deleteCode(email);
                throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "인증 시도 횟수를 초과했습니다.");
            }
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "인증 코드가 일치하지 않습니다.");
        }

        // Success -> Delete Code & Mark Verified
        sessionStoreRepository.deleteCode(email);
        String emailVerificationSessionId = sessionStoreRepository.createVerificationSession(
                email, Duration.ofSeconds(verifiedTtlSeconds)
        );

        return new VerifyEmailVerificationCodeResponseDto(emailVerificationSessionId);
    }
}
