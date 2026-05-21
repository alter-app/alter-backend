package com.dreamteam.alter.domain.notification.entity;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.notification.type.NotificationConsentType;
import com.dreamteam.alter.domain.notification.type.NotificationType;
import com.dreamteam.alter.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Table(name = "notification_consents")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
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

    @Column(name = "substitute_notification_consent", nullable = false)
    private boolean substituteNotificationConsent;

    @Column(name = "reputation_notification_consent", nullable = false)
    private boolean reputationNotificationConsent;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static NotificationConsent create(User user, boolean notificationConsent, boolean nightNotificationConsent) {
        return NotificationConsent.builder()
            .user(user)
            .notificationConsent(notificationConsent)
            .nightNotificationConsent(notificationConsent && nightNotificationConsent)
            .substituteNotificationConsent(notificationConsent)
            .reputationNotificationConsent(notificationConsent)
            .build();
    }

    public void updateConsent(NotificationConsentType type, boolean consent) {
        switch (type) {
            case GENERAL -> {
                this.notificationConsent = consent;
                if (!consent) {
                    this.nightNotificationConsent = false;
                    this.substituteNotificationConsent = false;
                    this.reputationNotificationConsent = false;
                }
            }
            case NIGHT -> {
                validateSubConsent(consent, "야간");
                this.nightNotificationConsent = consent;
            }
            case SUBSTITUTE -> {
                validateSubConsent(consent, "대타");
                this.substituteNotificationConsent = consent;
            }
            case REPUTATION -> {
                validateSubConsent(consent, "평판");
                this.reputationNotificationConsent = consent;
            }
        }
    }

    public boolean isBlocked(NotificationType type, boolean isDaytime) {
        Objects.requireNonNull(type, "notification type must not be null");
        if (!this.notificationConsent) {
            return true;
        }
        if (!isDaytime && !this.nightNotificationConsent) {
            return true;
        }
        return switch (type) {
            case SUBSTITUTE -> !this.substituteNotificationConsent;
            case REPUTATION -> !this.reputationNotificationConsent;
            case GENERAL, SCHEDULE, POSTING_APPLICATION, CHAT,
                 WORKSPACE_INVITATION, JOIN_REQUEST -> false;
        };
    }

    private void validateSubConsent(boolean consent, String label) {
        if (consent && !this.notificationConsent) {
            throw new CustomException(
                ErrorCode.ILLEGAL_ARGUMENT,
                String.format("전체 알림 수신 동의가 꺼진 상태에서는 %s 알림을 켤 수 없습니다.", label)
            );
        }
    }
}
