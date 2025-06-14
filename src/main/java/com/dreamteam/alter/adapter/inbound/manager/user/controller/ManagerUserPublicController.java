package com.dreamteam.alter.adapter.inbound.manager.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.LoginUserRequestDto;
import com.dreamteam.alter.domain.user.port.inbound.GenerateTokenUseCase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/managers")
@RequiredArgsConstructor
@Validated
public class ManagerUserPublicController implements ManagerUserPublicControllerSpec {

    @Resource(name = "managerGenerateToken")
    private final GenerateTokenUseCase generateToken;

    @Override
    @PostMapping("/login")
    public ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> loginManagerUser(LoginUserRequestDto request) {
        return ResponseEntity.ok(CommonApiResponse.of(generateToken.execute(request)));
    }

}
