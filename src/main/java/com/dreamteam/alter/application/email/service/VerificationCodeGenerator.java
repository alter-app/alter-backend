package com.dreamteam.alter.application.email.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class VerificationCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    public String generate() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}
