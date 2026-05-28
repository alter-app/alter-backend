package com.dreamteam.alter.adapter.outbound.notification.persistence;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.entity.Notification;
import com.dreamteam.alter.domain.notification.entity.QNotification;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationRepository;
import com.dreamteam.alter.domain.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJpaRepository notificationJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Notification save(Notification notification) {
        return notificationJpaRepository.save(notification);
    }

    @Override
    public List<Notification> saveAll(List<Notification> notifications) {
        return notificationJpaRepository.saveAll(notifications);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return notificationJpaRepository.findById(id);
    }

    @Override
    public void markAllAsRead(User targetUser, TokenScope scope) {
        QNotification notification = QNotification.notification;
        queryFactory.update(notification)
            .set(notification.isRead, true)
            .where(
                notification.targetUser.eq(targetUser),
                notification.scope.eq(scope),
                notification.isRead.isFalse()
            )
            .execute();
    }
}
