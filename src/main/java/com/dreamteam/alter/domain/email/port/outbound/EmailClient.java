package com.dreamteam.alter.domain.email.port.outbound;

public interface EmailClient {
    void sendVerificationCode(String toEmail, String code);
}
