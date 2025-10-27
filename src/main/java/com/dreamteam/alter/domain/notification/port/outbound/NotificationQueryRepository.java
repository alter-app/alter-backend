package com.dreamteam.alter.domain.notification.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.notification.persistence.readonly.NotificationResponse;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.entity.User;

import java.util.List;

public interface NotificationQueryRepository {
    List<NotificationResponse> getNotificationsWithCursor(
        CursorPageRequest<CursorDto> pageRequest,
        User targetUser,
        TokenScope scope
    );
    long getCountOfNotifications(User targetUser, TokenScope scope);
}
