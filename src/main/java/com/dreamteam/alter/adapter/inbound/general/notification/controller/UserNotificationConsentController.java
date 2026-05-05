package com.dreamteam.alter.adapter.inbound.general.notification.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.NotificationConsentResponseDto;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.UpdateNotificationConsentRequestDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.notification.command.GetNotificationConsentCommand;
import com.dreamteam.alter.domain.notification.command.UpdateNotificationConsentCommand;
import com.dreamteam.alter.domain.notification.port.inbound.GetNotificationConsentUseCase;
import com.dreamteam.alter.domain.notification.port.inbound.UpdateNotificationConsentUseCase;
import com.dreamteam.alter.domain.notification.result.GetNotificationConsentResult;
import com.dreamteam.alter.domain.user.context.AppActor;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/users/me")
@PreAuthorize("hasAnyRole('USER')")
@RequiredArgsConstructor
@Validated
public class UserNotificationConsentController implements UserNotificationConsentControllerSpec {

    @Resource(name = "getNotificationConsent")
    private final GetNotificationConsentUseCase getNotificationConsentUseCase;

    @Resource(name = "updateNotificationConsent")
    private final UpdateNotificationConsentUseCase updateNotificationConsentUseCase;

    @Override
    @GetMapping("/notification-consent")
    public ResponseEntity<CommonApiResponse<NotificationConsentResponseDto>> getNotificationConsent() {
        AppActor actor = AppActionContext.getInstance().getActor();
        GetNotificationConsentResult result = getNotificationConsentUseCase.execute(
            GetNotificationConsentCommand.from(actor.getUser())
        );
        return ResponseEntity.ok(CommonApiResponse.of(NotificationConsentResponseDto.from(result)));
    }

    @Override
    @PutMapping("/notification-consent")
    public ResponseEntity<CommonApiResponse<Void>> updateNotificationConsent(
        @Valid @RequestBody UpdateNotificationConsentRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        updateNotificationConsentUseCase.execute(
            UpdateNotificationConsentCommand.of(
                actor.getUser(),
                request.getType(),
                request.getConsent()
            )
        );
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
