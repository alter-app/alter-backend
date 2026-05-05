package com.dreamteam.alter.domain.notification.port.outbound;

import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import com.dreamteam.alter.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface NotificationConsentQueryRepository {
    Optional<NotificationConsent> findByUser(User user);
    List<NotificationConsent> findByUsers(List<User> users);
}
