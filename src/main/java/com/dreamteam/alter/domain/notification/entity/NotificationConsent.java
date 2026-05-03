package com.dreamteam.alter.domain.notification.entity;

import com.dreamteam.alter.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "notification_consents")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "notification_consent", nullable = false)
    private boolean notificationConsent;

    @Column(name = "night_notification_consent", nullable = false)
    private boolean nightNotificationConsent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static NotificationConsent create(User user, boolean notificationConsent, boolean nightNotificationConsent) {
        return NotificationConsent.builder()
            .user(user)
            .notificationConsent(notificationConsent)
            .nightNotificationConsent(nightNotificationConsent)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    public void updateConsent(boolean notificationConsent, boolean nightNotificationConsent) {
        this.notificationConsent = notificationConsent;
        this.nightNotificationConsent = nightNotificationConsent;
        this.updatedAt = LocalDateTime.now();
    }
}
