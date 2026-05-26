package com.dreamteam.alter.adapter.inbound.general.notification.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.MarkNotificationsAsReadRequestDto;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.NotificationListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.NotificationResponseDto;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.UnreadNotificationCountResponseDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.notification.port.inbound.GetMyNotificationsUseCase;
import com.dreamteam.alter.domain.notification.port.inbound.GetUnreadNotificationCountUseCase;
import com.dreamteam.alter.domain.notification.port.inbound.MarkNotificationsAsReadUseCase;
import com.dreamteam.alter.domain.user.context.AppActor;
import jakarta.annotation.Resource;
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
public class UserNotificationController implements UserNotificationControllerSpec {

    @Resource(name = "getMyNotifications")
    private final GetMyNotificationsUseCase getMyNotificationsUseCase;

    @Resource(name = "markNotificationsAsRead")
    private final MarkNotificationsAsReadUseCase markNotificationsAsReadUseCase;

    @Resource(name = "getUnreadNotificationCount")
    private final GetUnreadNotificationCountUseCase getUnreadNotificationCountUseCase;

    @Override
    @GetMapping("/notifications")
    public ResponseEntity<CursorPaginatedApiResponse<NotificationResponseDto>> getMyNotifications(
        CursorPageRequestDto pageRequest,
        NotificationListFilterDto filter
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        return ResponseEntity.ok(getMyNotificationsUseCase.execute(actor, pageRequest, filter));
    }

    @Override
    @PatchMapping("/notifications/read")
    public ResponseEntity<CommonApiResponse<Void>> markNotificationsAsRead(
        @RequestBody MarkNotificationsAsReadRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        markNotificationsAsReadUseCase.execute(actor, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping("/notifications/unread-count")
    public ResponseEntity<CommonApiResponse<UnreadNotificationCountResponseDto>> getUnreadNotificationCount() {
        AppActor actor = AppActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(getUnreadNotificationCountUseCase.execute(actor)));
    }
}
