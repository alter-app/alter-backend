package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.domain.user.port.inbound.ReissueTokenUseCase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/users")
@RequiredArgsConstructor
@Validated
public class UserController implements UserControllerSpec {

    @Resource(name = "reissueToken")
    private final ReissueTokenUseCase reissueToken;

    @Override
    @PostMapping("/token")
    public ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> reissueToken(Authentication authentication) {
        return ResponseEntity.ok(CommonApiResponse.of(reissueToken.execute(authentication)));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<CommonApiResponse<Void>> logoutUser(Authentication authentication) {
        return null;
    }

}
