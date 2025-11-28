package com.dreamteam.alter.adapter.inbound.manager.auth.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.port.inbound.LogoutManagerUseCase;
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
@RequestMapping("/manager/auth")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class ManagerAuthController implements ManagerAuthControllerSpec {

    @Resource(name = "reissueToken")
    private final ReissueTokenUseCase reissueToken;

    @Resource(name = "logoutManager")
    private final LogoutManagerUseCase logoutManager;

    @Override
    @PostMapping("/token")
    public ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> reissueToken(Authentication authentication) {
        return ResponseEntity.ok(CommonApiResponse.of(reissueToken.execute(authentication)));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<CommonApiResponse<Void>> logout() {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        logoutManager.execute(actor);
        return ResponseEntity.noContent().build();
    }

}
