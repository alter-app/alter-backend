package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.email.dto.SendEmailVerificationCodeRequestDto;
import com.dreamteam.alter.adapter.inbound.general.email.dto.VerifyEmailVerificationCodeRequestDto;
import com.dreamteam.alter.adapter.inbound.general.email.dto.VerifyEmailVerificationCodeResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.*;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.email.port.inbound.SendEmailVerificationCodeUseCase;
import com.dreamteam.alter.domain.email.port.inbound.VerifyEmailVerificationCodeUseCase;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.port.inbound.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/users/me")
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')") // TODO: 권한 세부 설정
@RequiredArgsConstructor
@Validated
public class UserSelfController implements UserSelfControllerSpec {

    @Resource(name = "getUserSelfInfo")
    private final GetUserSelfInfoUseCase getUserSelfInfo;

    @Resource(name = "addUserCertificate")
    private final AddUserCertificateUseCase addUserCertificate;

    @Resource(name = "getUserSelfCertificateList")
    private final GetUserSelfCertificateListUseCase getUserSelfCertificateList;

    @Resource(name = "getUserSelfCertificate")
    private final GetUserSelfCertificateUseCase getUserSelfCertificate;

    @Resource(name = "updateUserSelfCertificate")
    private final UpdateUserSelfCertificateUseCase updateUserSelfCertificate;

    @Resource(name = "deleteUserSelfCertificate")
    private final DeleteUserSelfCertificateUseCase deleteUserSelfCertificate;

    @Resource(name = "updateEmail")
    private final UpdateEmailUseCase updateEmail;

    @Resource(name = "removeEmail")
    private final RemoveEmailUseCase removeEmail;

    @Resource(name = "sendEmailVerificationCode")
    private final SendEmailVerificationCodeUseCase sendEmailVerificationCode;

    @Resource(name = "verifyEmailVerificationCode")
    private final VerifyEmailVerificationCodeUseCase verifyEmailVerificationCode;

    @Override
    @GetMapping
    public ResponseEntity<CommonApiResponse<UserSelfInfoResponseDto>> getUserSelfInfo() {
        AppActor actor = AppActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(getUserSelfInfo.execute(actor)));
    }

    @Override
    @PostMapping("/certificates")
    public ResponseEntity<CommonApiResponse<Void>> addUserCertificate(
        CreateUserCertificateRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        addUserCertificate.execute(request, actor);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping("/certificates")
    public ResponseEntity<CommonApiResponse<List<UserSelfCertificateListResponseDto>>> getUserSelfCertificateList() {
        AppActor actor = AppActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(getUserSelfCertificateList.execute(actor)));
    }

    @Override
    @GetMapping("/certificates/{certificateId}")
    public ResponseEntity<CommonApiResponse<UserSelfCertificateResponseDto>> getUserSelfCertificate(
        @PathVariable Long certificateId
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(getUserSelfCertificate.execute(actor, certificateId)));
    }

    @Override
    @PutMapping("/certificates/{certificateId}")
    public ResponseEntity<CommonApiResponse<Void>> updateUserSelfCertificate(
        @PathVariable Long certificateId,
        UpdateUserCertificateRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        updateUserSelfCertificate.execute(actor, certificateId, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @DeleteMapping("/certificates/{certificateId}")
    public ResponseEntity<CommonApiResponse<Void>> deleteUserSelfCertificate(
        @PathVariable Long certificateId
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        deleteUserSelfCertificate.execute(actor, certificateId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PostMapping("/email")
    public ResponseEntity<CommonApiResponse<Void>> updateEmail(
        @Valid @RequestBody RegisterEmailRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        updateEmail.execute(actor, request.getEmailVerificationSessionId());
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @DeleteMapping("/email")
    public ResponseEntity<CommonApiResponse<Void>> removeEmail() {
        AppActor actor = AppActionContext.getInstance().getActor();

        removeEmail.execute(actor);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PostMapping("/email/verification/send")
    public ResponseEntity<CommonApiResponse<Void>> sendVerificationCode(
            @Valid @RequestBody SendEmailVerificationCodeRequestDto request
    ) {
        sendEmailVerificationCode.execute(request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PostMapping("/email/verify")
    public ResponseEntity<CommonApiResponse<VerifyEmailVerificationCodeResponseDto>> verifyVerificationCode(
            @Valid @RequestBody VerifyEmailVerificationCodeRequestDto request
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(verifyEmailVerificationCode.execute(request)));
    }

}
