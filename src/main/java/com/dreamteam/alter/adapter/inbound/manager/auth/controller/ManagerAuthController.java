package com.dreamteam.alter.adapter.inbound.manager.auth.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.FirebaseCustomTokenResponseDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.port.inbound.ManagerGenerateFirebaseCustomTokenUseCase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager/auth")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class ManagerAuthController implements ManagerAuthControllerSpec {

    @Resource(name = "managerGenerateFirebaseCustomToken")
    private final ManagerGenerateFirebaseCustomTokenUseCase managerGenerateFirebaseCustomToken;

    @Override
    @GetMapping("/firebase-token")
    public ResponseEntity<CommonApiResponse<FirebaseCustomTokenResponseDto>> generateFirebaseCustomToken() {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(managerGenerateFirebaseCustomToken.execute(actor)));
    }

}
