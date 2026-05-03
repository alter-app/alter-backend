package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.email.dto.SendEmailVerificationCodeRequestDto;
import com.dreamteam.alter.adapter.inbound.general.email.dto.VerifyEmailVerificationCodeRequestDto;
import com.dreamteam.alter.adapter.inbound.general.email.dto.VerifyEmailVerificationCodeResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.*;
import com.dreamteam.alter.domain.email.command.SendEmailVerificationCodeCommand;
import com.dreamteam.alter.domain.email.command.VerifyEmailVerificationCodeCommand;
import com.dreamteam.alter.domain.email.port.inbound.SendEmailVerificationCodeUseCase;
import com.dreamteam.alter.domain.email.port.inbound.VerifyEmailVerificationCodeUseCase;
import com.dreamteam.alter.domain.user.port.inbound.CreateSignupSessionUseCase;
import com.dreamteam.alter.domain.user.port.inbound.LoginWithPasswordUseCase;
import com.dreamteam.alter.domain.user.port.inbound.LoginWithSocialUseCase;
import com.dreamteam.alter.domain.user.port.inbound.CreateUserUseCase;
import com.dreamteam.alter.domain.user.port.inbound.CreateUserWithSocialUseCase;
import com.dreamteam.alter.domain.user.port.inbound.CheckContactDuplicationUseCase;
import com.dreamteam.alter.domain.user.port.inbound.CheckNicknameDuplicationUseCase;
import com.dreamteam.alter.domain.user.port.inbound.CheckEmailDuplicationUseCase;
import com.dreamteam.alter.domain.user.port.inbound.CreatePasswordResetSessionUseCase;
import com.dreamteam.alter.domain.user.port.inbound.ResetPasswordUseCase;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/users")
@RequiredArgsConstructor
@Validated
public class UserPublicController implements UserPublicControllerSpec {

    @Resource(name = "createSignupSession")
    private final CreateSignupSessionUseCase createSignupSession;

    @Resource(name = "loginWithPassword")
    private final LoginWithPasswordUseCase loginWithPassword;

    @Resource(name = "loginWithSocial")
    private final LoginWithSocialUseCase loginWithSocial;

    @Resource(name = "createUser")
    private final CreateUserUseCase createUser;

    @Resource(name = "createUserWithSocial")
    private final CreateUserWithSocialUseCase createUserWithSocial;

    @Resource(name = "checkContactDuplication")
    private final CheckContactDuplicationUseCase checkContactDuplication;

    @Resource(name = "checkNicknameDuplication")
    private final CheckNicknameDuplicationUseCase checkNicknameDuplication;

    @Resource(name = "checkEmailDuplication")
    private final CheckEmailDuplicationUseCase checkEmailDuplication;

    @Resource(name = "createPasswordResetSession")
    private final CreatePasswordResetSessionUseCase createPasswordResetSession;

    @Resource(name = "resetPassword")
    private final ResetPasswordUseCase resetPassword;

    @Resource(name = "sendEmailVerificationCode")
    private final SendEmailVerificationCodeUseCase sendEmailVerificationCode;

    @Resource(name = "verifyEmailVerificationCode")
    private final VerifyEmailVerificationCodeUseCase verifyEmailVerificationCode;


    @Override
    @PostMapping("/signup-session")
    public ResponseEntity<CommonApiResponse<CreateSignupSessionResponseDto>> createSignupSession(
        @Valid @RequestBody CreateSignupSessionRequestDto request
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(createSignupSession.execute(request)));
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> loginWithPassword(
        @Valid @RequestBody LoginWithPasswordRequestDto request
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(loginWithPassword.execute(request)));
    }

    @Override
    @PostMapping("/login-social")
    public ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> loginWithSocial(
        @Valid @RequestBody SocialLoginRequestDto request
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(loginWithSocial.execute(request)));
    }

    @Override
    @PostMapping("/signup")
    public ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> createUser(
        @Valid @RequestBody CreateUserRequestDto request
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(createUser.execute(request)));
    }

    @Override
    @PostMapping("/signup-social")
    public ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> createUserWithSocial(
        @Valid @RequestBody CreateUserWithSocialRequestDto request
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(createUserWithSocial.execute(request)));
    }

    @Override
    @PostMapping("/exists/nickname")
    public ResponseEntity<CommonApiResponse<CheckNicknameDuplicationResponseDto>> checkNicknameDuplication(
        @Valid @RequestBody CheckNicknameDuplicationRequestDto request
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(checkNicknameDuplication.execute(request)));
    }

    @Override
    @PostMapping("/exists/contact")
    public ResponseEntity<CommonApiResponse<CheckContactDuplicationResponseDto>> checkContactDuplication(
        @Valid @RequestBody CheckContactDuplicationRequestDto request
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(checkContactDuplication.execute(request)));
    }

    @Override
    @PostMapping("/exists/email")
    public ResponseEntity<CommonApiResponse<CheckEmailDuplicationResponseDto>> checkEmailDuplication(
        @Valid @RequestBody CheckEmailDuplicationRequestDto request
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(checkEmailDuplication.execute(request)));
    }

    @Override
    @PostMapping("/password-reset/session")
    public ResponseEntity<CommonApiResponse<CreatePasswordResetSessionResponseDto>> createPasswordResetSession(
        @Valid @RequestBody CreatePasswordResetSessionRequestDto request
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(createPasswordResetSession.execute(request)));
    }

    @Override
    @PostMapping("/password-reset")
    public ResponseEntity<CommonApiResponse<Void>> resetPassword(
        @Valid @RequestBody ResetPasswordRequestDto request
    ) {
        resetPassword.execute(request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PostMapping("/email/verification/send")
    public ResponseEntity<CommonApiResponse<Void>> sendSignupEmailVerificationCode(
        @Valid @RequestBody SendEmailVerificationCodeRequestDto request
    ) {
        sendEmailVerificationCode.execute(new SendEmailVerificationCodeCommand(request.getEmail()));
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PostMapping("/email/verification")
    public ResponseEntity<CommonApiResponse<VerifyEmailVerificationCodeResponseDto>> verifySignupEmailVerificationCode(
        @Valid @RequestBody VerifyEmailVerificationCodeRequestDto request
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(
            VerifyEmailVerificationCodeResponseDto.from(
                verifyEmailVerificationCode.execute(new VerifyEmailVerificationCodeCommand(request.getEmail(), request.getCode()))
            )
        ));
    }
}
