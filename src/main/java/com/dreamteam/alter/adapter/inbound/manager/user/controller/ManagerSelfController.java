package com.dreamteam.alter.adapter.inbound.manager.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.email.dto.SendEmailVerificationCodeRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.email.dto.VerifyEmailVerificationCodeRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.email.dto.VerifyEmailVerificationCodeResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.user.dto.CreateManagerProfileImageRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.user.dto.ManagerSelfInfoResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.user.dto.RegisterEmailRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.user.dto.UpdateManagerProfileImageRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.user.dto.UpdateNicknameRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.user.dto.UpdatePasswordRequestDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.email.command.SendEmailVerificationCodeCommand;
import com.dreamteam.alter.domain.email.command.VerifyEmailVerificationCodeCommand;
import com.dreamteam.alter.domain.email.port.inbound.SendEmailVerificationCodeUseCase;
import com.dreamteam.alter.domain.email.port.inbound.VerifyEmailVerificationCodeUseCase;
import com.dreamteam.alter.domain.user.command.GetUserSelfInfoCommand;
import com.dreamteam.alter.domain.user.command.RemoveEmailCommand;
import com.dreamteam.alter.domain.user.command.UpdateEmailCommand;
import com.dreamteam.alter.domain.user.command.UpdateNicknameCommand;
import com.dreamteam.alter.domain.user.command.UpdatePasswordCommand;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.CreateUserProfileImageUseCase;
import com.dreamteam.alter.domain.user.port.inbound.DeleteUserProfileImageUseCase;
import com.dreamteam.alter.domain.user.port.inbound.GetUserSelfInfoUseCase;
import com.dreamteam.alter.domain.user.port.inbound.RemoveEmailUseCase;
import com.dreamteam.alter.domain.user.port.inbound.UpdateEmailUseCase;
import com.dreamteam.alter.domain.user.port.inbound.UpdateNicknameUseCase;
import com.dreamteam.alter.domain.user.port.inbound.UpdatePasswordUseCase;
import com.dreamteam.alter.domain.user.port.inbound.UpdateUserProfileImageUseCase;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/manager/me")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class ManagerSelfController implements ManagerSelfControllerSpec {

    @Resource(name = "getUserSelfInfo")
    private final GetUserSelfInfoUseCase getUserSelfInfo;

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
    public ResponseEntity<CommonApiResponse<ManagerSelfInfoResponseDto>> getManagerSelfInfo() {
        User user = currentUser();

        return ResponseEntity.ok(CommonApiResponse.of(
            ManagerSelfInfoResponseDto.from(getUserSelfInfo.execute(new GetUserSelfInfoCommand(user)))
        ));
    }

    @Override
    @PostMapping("/email")
    public ResponseEntity<CommonApiResponse<Void>> updateEmail(
        @Valid @RequestBody RegisterEmailRequestDto request
    ) {
        updateEmail.execute(new UpdateEmailCommand(currentUser(), request.getSessionId()));
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @DeleteMapping("/email")
    public ResponseEntity<CommonApiResponse<Void>> removeEmail() {
        removeEmail.execute(new RemoveEmailCommand(currentUser()));
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
        updatePassword.execute(new UpdatePasswordCommand(currentUser(), request.getCurrentPassword(), request.getNewPassword()));
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PutMapping("/nickname")
    public ResponseEntity<CommonApiResponse<Void>> updateNickname(
        @Valid @RequestBody UpdateNicknameRequestDto request
    ) {
        updateNickname.execute(new UpdateNicknameCommand(currentUser(), request.getNickname()));
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PostMapping("/profile-image")
    public ResponseEntity<CommonApiResponse<Void>> createProfileImage(
        @Valid @RequestBody CreateManagerProfileImageRequestDto request
    ) {
        createUserProfileImage.execute(currentUser(), request.getFileId());
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PutMapping("/profile-image")
    public ResponseEntity<CommonApiResponse<Void>> updateProfileImage(
        @Valid @RequestBody UpdateManagerProfileImageRequestDto request
    ) {
        updateUserProfileImage.execute(currentUser(), request.getFileId());
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @DeleteMapping("/profile-image")
    public ResponseEntity<CommonApiResponse<Void>> deleteProfileImage() {
        deleteUserProfileImage.execute(currentUser());
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    private User currentUser() {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        return actor.getManagerUser().getUser();
    }
}
