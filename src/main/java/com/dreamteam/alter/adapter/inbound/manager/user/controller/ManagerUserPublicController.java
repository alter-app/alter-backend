package com.dreamteam.alter.adapter.inbound.manager.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.SocialLoginRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.LoginWithPasswordRequestDto;
import com.dreamteam.alter.domain.user.port.inbound.ManagerLoginWithSocialUseCase;
import com.dreamteam.alter.domain.user.port.inbound.ManagerLoginWithPasswordUseCase;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/managers")
@RequiredArgsConstructor
@Validated
public class ManagerUserPublicController implements ManagerUserPublicControllerSpec {

    @Resource(name = "managerLoginWithSocial")
    private final ManagerLoginWithSocialUseCase managerLoginWithSocial;

    @Resource(name = "managerLoginWithPassword")
    private final ManagerLoginWithPasswordUseCase managerLoginWithPassword;

    @Override
    @PostMapping("/login-social")
    public ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> loginManagerUserSocial(@Valid @RequestBody SocialLoginRequestDto request) {
        return ResponseEntity.ok(CommonApiResponse.of(managerLoginWithSocial.execute(request)));
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> loginManagerUser(@Valid @RequestBody LoginWithPasswordRequestDto request) {
        return ResponseEntity.ok(CommonApiResponse.of(managerLoginWithPassword.execute(request)));
    }
}
