package com.dreamteam.alter.domain.email.port.outbound;

import java.time.Duration;
import java.util.Optional;

public interface EmailVerificationTokenStorePort {
    void saveCode(String email, String code, Duration ttl);
    Optional<String> findCode(String email);
    void deleteCode(String email);

    void markVerified(String email, Duration ttl);
    boolean isVerified(String email);

    boolean isCooldown(String email);
    void markCooldown(String email, Duration ttl);

    long incrementAttempt(String email, Duration ttl);
}
