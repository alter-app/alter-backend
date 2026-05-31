package com.dreamteam.alter.domain.notification.port.outbound;

import com.dreamteam.alter.domain.notification.entity.Notification;

import java.util.List;

public interface NotificationRepository {
    Notification save(Notification notification);
    List<Notification> saveAll(List<Notification> notifications);
}
