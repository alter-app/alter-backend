package com.dreamteam.alter.adapter.inbound.manager.notification.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.NotificationConsentResponseDto;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.UpdateNotificationConsentRequestDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.notification.command.GetNotificationConsentCommand;
import com.dreamteam.alter.domain.notification.command.UpdateNotificationConsentCommand;
import com.dreamteam.alter.domain.notification.port.inbound.GetNotificationConsentUseCase;
import com.dreamteam.alter.domain.notification.port.inbound.UpdateNotificationConsentUseCase;
import com.dreamteam.alter.domain.notification.result.GetNotificationConsentResult;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manager/me")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class ManagerNotificationConsentController implements ManagerNotificationConsentControllerSpec {

    @Resource(name = "getNotificationConsent")
    private final GetNotificationConsentUseCase getNotificationConsentUseCase;

    @Resource(name = "updateNotificationConsent")
    private final UpdateNotificationConsentUseCase updateNotificationConsentUseCase;

    @Override
    @GetMapping("/notification-consent")
    public ResponseEntity<CommonApiResponse<NotificationConsentResponseDto>> getNotificationConsent() {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        GetNotificationConsentResult result = getNotificationConsentUseCase.execute(
            GetNotificationConsentCommand.from(actor.getManagerUser().getUser())
        );
        return ResponseEntity.ok(CommonApiResponse.of(NotificationConsentResponseDto.from(result)));
    }

    @Override
    @PutMapping("/notification-consent")
    public ResponseEntity<CommonApiResponse<Void>> updateNotificationConsent(
        @Valid @RequestBody UpdateNotificationConsentRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        updateNotificationConsentUseCase.execute(
            UpdateNotificationConsentCommand.of(
                actor.getManagerUser().getUser(),
                request.getType(),
                request.getConsent()
            )
        );
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
