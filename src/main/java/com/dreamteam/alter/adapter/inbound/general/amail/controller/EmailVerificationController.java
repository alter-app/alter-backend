package com.dreamteam.alter.adapter.inbound.general.amail.controller;

import com.dreamteam.alter.adapter.inbound.general.amail.dto.SendEmailVerificationCodeRequestDto;
import com.dreamteam.alter.adapter.inbound.general.amail.dto.VerifyEmailVerificationCodeRequestDto;
import com.dreamteam.alter.domain.email.port.inbound.SendEmailVerificationCodeUseCase;
import com.dreamteam.alter.domain.email.port.inbound.VerifyEmailVerificationCodeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/auth/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    @Resource(name = "sendEmailVerificationCode")
    private final SendEmailVerificationCodeUseCase sendEmailVerificationCode;

    @Resource(name = "verifyEmailVerificationCode")
    private final VerifyEmailVerificationCodeUseCase verifyEmailVerificationCode;

    @Operation(summary = "이메일 인증 코드 발송", description = "이메일로 6자리 인증 코드를 발송")
    @PostMapping("/send")
    public ResponseEntity<Void> sendVerificationCode(@RequestBody @Valid SendEmailVerificationCodeRequestDto request) {
        sendEmailVerificationCode.execute(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "이메일 인증 코드 검증", description = "발송된 인증 코드를 검증합니다.")
    @PostMapping("/verify")
    public ResponseEntity<Void> verifyVerificationCode(@RequestBody @Valid VerifyEmailVerificationCodeRequestDto request) {
        verifyEmailVerificationCode.execute(request);
        return ResponseEntity.ok().build();
    }
}
