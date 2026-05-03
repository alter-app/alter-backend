package com.dreamteam.alter.adapter.inbound.general.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.email.dto.SendEmailVerificationCodeRequestDto;
import com.dreamteam.alter.adapter.inbound.general.email.dto.VerifyEmailVerificationCodeRequestDto;
import com.dreamteam.alter.adapter.inbound.general.email.dto.VerifyEmailVerificationCodeResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserCertificateRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserProfileImageRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.RegisterEmailRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UpdateNicknameRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UpdatePasswordRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UpdateUserCertificateRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UpdateUserProfileImageRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserSelfCertificateListResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserSelfCertificateResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserSelfInfoResponseDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.email.command.SendEmailVerificationCodeCommand;
import com.dreamteam.alter.domain.email.command.VerifyEmailVerificationCodeCommand;
import com.dreamteam.alter.domain.email.port.inbound.SendEmailVerificationCodeUseCase;
import com.dreamteam.alter.domain.email.port.inbound.VerifyEmailVerificationCodeUseCase;
import com.dreamteam.alter.domain.user.command.GetUserSelfInfoCommand;
import com.dreamteam.alter.domain.user.command.RemoveEmailCommand;
import com.dreamteam.alter.domain.user.command.UpdateEmailCommand;
import com.dreamteam.alter.domain.user.command.UpdateNicknameCommand;
import com.dreamteam.alter.domain.user.command.UpdatePasswordCommand;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.port.inbound.AddUserCertificateUseCase;
import com.dreamteam.alter.domain.user.port.inbound.CreateUserProfileImageUseCase;
import com.dreamteam.alter.domain.user.port.inbound.DeleteUserProfileImageUseCase;
import com.dreamteam.alter.domain.user.port.inbound.DeleteUserSelfCertificateUseCase;
import com.dreamteam.alter.domain.user.port.inbound.GetUserSelfCertificateListUseCase;
import com.dreamteam.alter.domain.user.port.inbound.GetUserSelfCertificateUseCase;
import com.dreamteam.alter.domain.user.port.inbound.GetUserSelfInfoUseCase;
import com.dreamteam.alter.domain.user.port.inbound.RemoveEmailUseCase;
import com.dreamteam.alter.domain.user.port.inbound.UpdateEmailUseCase;
import com.dreamteam.alter.domain.user.port.inbound.UpdateNicknameUseCase;
import com.dreamteam.alter.domain.user.port.inbound.UpdatePasswordUseCase;
import com.dreamteam.alter.domain.user.port.inbound.UpdateUserProfileImageUseCase;
import com.dreamteam.alter.domain.user.port.inbound.UpdateUserSelfCertificateUseCase;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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

    @Resource(name = "updatePassword")
    private final UpdatePasswordUseCase updatePassword;

    @Resource(name = "updateNickname")
    private final UpdateNicknameUseCase updateNickname;

    @Resource(name = "createUserProfileImage")
    private final CreateUserProfileImageUseCase createUserProfileImage;

    @Resource(name = "updateUserProfileImage")
    private final UpdateUserProfileImageUseCase updateUserProfileImage;

    @Resource(name = "deleteUserProfileImage")
    private final DeleteUserProfileImageUseCase deleteUserProfileImage;

    @Override
    @GetMapping
    public ResponseEntity<CommonApiResponse<UserSelfInfoResponseDto>> getUserSelfInfo() {
        AppActor actor = AppActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(
            UserSelfInfoResponseDto.from(getUserSelfInfo.execute(new GetUserSelfInfoCommand(actor.getUser())))
        ));
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

        updateEmail.execute(new UpdateEmailCommand(actor.getUser(), request.getSessionId()));
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @DeleteMapping("/email")
    public ResponseEntity<CommonApiResponse<Void>> removeEmail() {
        AppActor actor = AppActionContext.getInstance().getActor();

        removeEmail.execute(new RemoveEmailCommand(actor.getUser()));
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PostMapping("/email/verification/send")
    public ResponseEntity<CommonApiResponse<Void>> sendVerificationCode(
            @Valid @RequestBody SendEmailVerificationCodeRequestDto request
    ) {
        sendEmailVerificationCode.execute(new SendEmailVerificationCodeCommand(request.getEmail()));
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PostMapping("/email/verification")
    public ResponseEntity<CommonApiResponse<VerifyEmailVerificationCodeResponseDto>> verifyVerificationCode(
            @Valid @RequestBody VerifyEmailVerificationCodeRequestDto request
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(
            VerifyEmailVerificationCodeResponseDto.from(
                verifyEmailVerificationCode.execute(new VerifyEmailVerificationCodeCommand(request.getEmail(), request.getCode()))
            )
        ));
    }

    @Override
    @PutMapping("/password")
    public ResponseEntity<CommonApiResponse<Void>> updatePassword(
        @Valid @RequestBody UpdatePasswordRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        updatePassword.execute(new UpdatePasswordCommand(actor.getUser(), request.getCurrentPassword(), request.getNewPassword()));
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PutMapping("/nickname")
    public ResponseEntity<CommonApiResponse<Void>> updateNickname(
        @Valid @RequestBody UpdateNicknameRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        updateNickname.execute(new UpdateNicknameCommand(actor.getUser(), request.getNickname()));
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PostMapping("/profile-image")
    public ResponseEntity<CommonApiResponse<Void>> createProfileImage(
        @Valid @RequestBody CreateUserProfileImageRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        createUserProfileImage.execute(actor.getUser(), request.getFileId());
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PutMapping("/profile-image")
    public ResponseEntity<CommonApiResponse<Void>> updateProfileImage(
        @Valid @RequestBody UpdateUserProfileImageRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        updateUserProfileImage.execute(actor.getUser(), request.getFileId());
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @DeleteMapping("/profile-image")
    public ResponseEntity<CommonApiResponse<Void>> deleteProfileImage() {
        AppActor actor = AppActionContext.getInstance().getActor();
        deleteUserProfileImage.execute(actor.getUser());
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
