package com.dreamteam.alter.domain.notification.port.outbound;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.entity.Notification;
import com.dreamteam.alter.domain.user.entity.User;

import java.util.List;

public interface NotificationRepository {
    Notification save(Notification notification);
    List<Notification> saveAll(List<Notification> notifications);
    void markAllAsRead(User targetUser, TokenScope scope);
}
