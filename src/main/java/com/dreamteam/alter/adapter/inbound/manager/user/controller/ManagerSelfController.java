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
import com.dreamteam.alter.adapter.inbound.manager.user.dto.CreateManagerProfileImageRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.user.dto.ManagerSelfInfoResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.user.dto.UpdateManagerProfileImageRequestDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.port.inbound.CreateUserProfileImageUseCase;
import com.dreamteam.alter.domain.user.port.inbound.DeleteUserProfileImageUseCase;
import com.dreamteam.alter.domain.user.port.inbound.GetManagerSelfInfoUseCase;
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

    @Resource(name = "getManagerSelfInfo")
    private final GetManagerSelfInfoUseCase getManagerSelfInfo;

    @Resource(name = "createUserProfileImage")
    private final CreateUserProfileImageUseCase createUserProfileImage;

    @Resource(name = "updateUserProfileImage")
    private final UpdateUserProfileImageUseCase updateUserProfileImage;

    @Resource(name = "deleteUserProfileImage")
    private final DeleteUserProfileImageUseCase deleteUserProfileImage;

    @Override
    @GetMapping
    public ResponseEntity<CommonApiResponse<ManagerSelfInfoResponseDto>> getManagerSelfInfo() {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(getManagerSelfInfo.execute(actor)));
    }

    @Override
    @PostMapping("/profile-image")
    public ResponseEntity<CommonApiResponse<Void>> createProfileImage(
        @Valid @RequestBody CreateManagerProfileImageRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        createUserProfileImage.execute(actor.getManagerUser().getUser(), request.getFileId());
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PutMapping("/profile-image")
    public ResponseEntity<CommonApiResponse<Void>> updateProfileImage(
        @Valid @RequestBody UpdateManagerProfileImageRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        updateUserProfileImage.execute(actor.getManagerUser().getUser(), request.getFileId());
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @DeleteMapping("/profile-image")
    public ResponseEntity<CommonApiResponse<Void>> deleteProfileImage() {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        deleteUserProfileImage.execute(actor.getManagerUser().getUser());
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
