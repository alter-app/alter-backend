package com.dreamteam.alter.adapter.inbound.manager.notification.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.NotificationResponseDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.notification.port.inbound.ManagerGetMyNotificationsUseCase;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager/notifications")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class ManagerNotificationController implements ManagerNotificationControllerSpec {

    @Resource(name = "managerGetMyNotifications")
    private final ManagerGetMyNotificationsUseCase managerGetMyNotificationsUseCase;

    @Override
    @GetMapping("/me")
    public ResponseEntity<CursorPaginatedApiResponse<NotificationResponseDto>> getMyNotifications(
        CursorPageRequestDto pageRequest
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        return ResponseEntity.ok(managerGetMyNotificationsUseCase.execute(actor, pageRequest));
    }
}
