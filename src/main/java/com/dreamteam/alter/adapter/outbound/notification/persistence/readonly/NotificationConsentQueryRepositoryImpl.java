package com.dreamteam.alter.adapter.outbound.notification.persistence.readonly;

import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import com.dreamteam.alter.domain.notification.entity.QNotificationConsent;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationConsentQueryRepository;
import com.dreamteam.alter.domain.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationConsentQueryRepositoryImpl implements NotificationConsentQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<NotificationConsent> findByUser(User user) {
        QNotificationConsent qNotificationConsent = new QNotificationConsent("notificationConsent");

        return Optional.ofNullable(queryFactory
            .selectFrom(qNotificationConsent)
            .where(qNotificationConsent.user.eq(user))
            .fetchOne());
    }

    @Override
    public List<NotificationConsent> findByUsers(List<User> users) {
        if (ObjectUtils.isEmpty(users)) {
            return List.of();
        }

        QNotificationConsent qNotificationConsent = new QNotificationConsent("notificationConsent");

        return queryFactory
            .selectFrom(qNotificationConsent)
            .where(qNotificationConsent.user.in(users))
            .fetch();
    }
}
