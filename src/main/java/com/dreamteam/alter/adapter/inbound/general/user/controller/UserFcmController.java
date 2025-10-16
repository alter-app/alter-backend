package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.RegisterFcmDeviceTokenRequestDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.port.inbound.RegisterFcmDeviceTokenUseCase;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/users")
@PreAuthorize("hasAnyRole('USER', 'MANAGER')")
@RequiredArgsConstructor
@Validated
public class UserFcmController implements UserFcmControllerSpec {

    @Resource(name = "registerFCMDeviceToken")
    private final RegisterFcmDeviceTokenUseCase registerFCMDeviceToken;

    @Override
    @PostMapping("/device-token")
    public ResponseEntity<CommonApiResponse<Void>> registerDeviceToken(
        @Valid @RequestBody RegisterFcmDeviceTokenRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance()
            .getActor();

        registerFCMDeviceToken.execute(actor, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
