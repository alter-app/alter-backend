package com.dreamteam.alter.adapter.outbound.notification.persistence;

import com.dreamteam.alter.domain.notification.entity.Notification;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJpaRepository notificationJpaRepository;

    @Override
    public Notification save(Notification notification) {
        return notificationJpaRepository.save(notification);
    }

    @Override
    public List<Notification> saveAll(List<Notification> notifications) {
        return notificationJpaRepository.saveAll(notifications);
    }
}
