package com.dreamteam.alter.domain.email.port.outbound;

import java.time.Duration;
import java.util.Optional;

public interface EmailVerificationSessionStoreRepository {
    // --- 인증 코드 관련 ---
    void saveCode(String email, String code, Duration ttl);
    Optional<String> findCode(String email);
    void deleteCode(String email);
    long incrementAttempt(String email, Duration ttl);

    // --- 쿨다운 관련 ---
    boolean isCooldown(String email);
    void markCooldown(String email, Duration ttl);

    // -- 인증 세션 관련 ---
    String createVerificationSession(String email, Duration ttl);
    Optional<String> getEmailBySession(String token);
    void deleteSession(String token);

}
