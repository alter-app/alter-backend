package com.dreamteam.alter.adapter.outbound.notification.persistence;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.entity.Notification;
import com.dreamteam.alter.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.targetUser = :targetUser AND n.scope = :scope AND n.isRead = false")
    void markAllAsRead(@Param("targetUser") User targetUser, @Param("scope") TokenScope scope);
}
