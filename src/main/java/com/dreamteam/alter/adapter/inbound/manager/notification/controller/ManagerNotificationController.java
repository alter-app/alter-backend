package com.dreamteam.alter.adapter.inbound.manager.notification.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.MarkNotificationsAsReadRequestDto;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.NotificationListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.NotificationResponseDto;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.UnreadNotificationCountResponseDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.notification.port.inbound.ManagerGetMyNotificationsUseCase;
import com.dreamteam.alter.domain.notification.port.inbound.ManagerGetUnreadNotificationCountUseCase;
import com.dreamteam.alter.domain.notification.port.inbound.ManagerMarkNotificationsAsReadUseCase;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manager/notifications")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class ManagerNotificationController implements ManagerNotificationControllerSpec {

    @Resource(name = "managerGetMyNotifications")
    private final ManagerGetMyNotificationsUseCase managerGetMyNotificationsUseCase;

    @Resource(name = "managerMarkNotificationsAsRead")
    private final ManagerMarkNotificationsAsReadUseCase managerMarkNotificationsAsReadUseCase;

    @Resource(name = "managerGetUnreadNotificationCount")
    private final ManagerGetUnreadNotificationCountUseCase managerGetUnreadNotificationCountUseCase;

    @Override
    @GetMapping("/me")
    public ResponseEntity<CursorPaginatedApiResponse<NotificationResponseDto>> getMyNotifications(
        CursorPageRequestDto pageRequest,
        NotificationListFilterDto filter
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        return ResponseEntity.ok(managerGetMyNotificationsUseCase.execute(actor, pageRequest, filter));
    }

    @Override
    @PatchMapping("/read")
    public ResponseEntity<CommonApiResponse<Void>> markNotificationsAsRead(
        @RequestBody(required = false) MarkNotificationsAsReadRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        managerMarkNotificationsAsReadUseCase.execute(actor, request != null ? request.getNotificationId() : null);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping("/me/unread-count")
    public ResponseEntity<CommonApiResponse<UnreadNotificationCountResponseDto>> getUnreadNotificationCount() {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(UnreadNotificationCountResponseDto.of(managerGetUnreadNotificationCountUseCase.execute(actor))));
    }
}
