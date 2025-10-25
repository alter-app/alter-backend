package com.dreamteam.alter.adapter.inbound.general.auth.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.port.inbound.LogoutUserUseCase;
import com.dreamteam.alter.domain.user.port.inbound.ReissueTokenUseCase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/auth")
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')") // TODO: 권한 세부 설정
@RequiredArgsConstructor
@Validated
public class AuthController implements AuthControllerSpec {

    @Resource(name = "reissueToken")
    private final ReissueTokenUseCase reissueToken;

    @Resource(name = "logoutUser")
    private final LogoutUserUseCase logoutUser;

    @Override
    @PostMapping("/token")
    public ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> reissueToken(Authentication authentication) {
        return ResponseEntity.ok(CommonApiResponse.of(reissueToken.execute(authentication)));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<CommonApiResponse<Void>> logout() {
        AppActor actor = AppActionContext.getInstance().getActor();

        logoutUser.execute(actor);
        return ResponseEntity.noContent().build();
    }

}
