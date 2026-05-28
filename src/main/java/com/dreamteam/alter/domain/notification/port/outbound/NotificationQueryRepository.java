package com.dreamteam.alter.domain.notification.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.notification.persistence.readonly.NotificationResponse;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.entity.Notification;
import com.dreamteam.alter.domain.notification.type.NotificationType;
import com.dreamteam.alter.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface NotificationQueryRepository {
    Optional<Notification> findById(Long id);
    List<NotificationResponse> getNotificationsWithCursor(
        CursorPageRequest<CursorDto> pageRequest,
        User targetUser,
        TokenScope scope,
        NotificationType type,
        Boolean isRead
    );
    long getCountOfNotifications(User targetUser, TokenScope scope, NotificationType type, Boolean isRead);
    long getCountOfUnreadNotifications(User targetUser, TokenScope scope);
}
