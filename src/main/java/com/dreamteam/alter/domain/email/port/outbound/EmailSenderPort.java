package com.dreamteam.alter.domain.email.port.outbound;

public interface EmailSenderPort {
    void sendVerificationCode(String toEmail, String code);
}
