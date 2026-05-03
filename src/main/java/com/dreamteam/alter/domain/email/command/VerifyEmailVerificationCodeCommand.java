package com.dreamteam.alter.domain.email.command;

public record VerifyEmailVerificationCodeCommand(String email, String code) {
}
