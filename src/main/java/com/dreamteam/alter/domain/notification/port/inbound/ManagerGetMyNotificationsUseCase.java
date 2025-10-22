package com.dreamteam.alter.domain.notification.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.NotificationResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetMyNotificationsUseCase {
    CursorPaginatedApiResponse<NotificationResponseDto> execute(ManagerActor actor, CursorPageRequestDto pageRequest);
}
