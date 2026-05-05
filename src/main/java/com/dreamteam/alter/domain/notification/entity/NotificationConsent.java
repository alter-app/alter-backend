package com.dreamteam.alter.domain.notification.entity;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.notification.type.NotificationConsentType;
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
            .build();
    }

    public void updateConsent(NotificationConsentType type, boolean consent) {
        switch (type) {
            case GENERAL -> {
                this.notificationConsent = consent;
                if (!consent) {
                    this.nightNotificationConsent = false;
                }
            }
            case NIGHT -> {
                if (consent && !this.notificationConsent) {
                    throw new CustomException(
                        ErrorCode.ILLEGAL_ARGUMENT,
                        "전체 알림 수신 동의가 꺼진 상태에서는 야간 알림을 켤 수 없습니다."
                    );
                }
                this.nightNotificationConsent = consent;
            }
        }
    }
}
