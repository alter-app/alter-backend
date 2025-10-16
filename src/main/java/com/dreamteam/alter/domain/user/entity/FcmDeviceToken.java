package com.dreamteam.alter.domain.user.entity;

import com.dreamteam.alter.domain.user.type.DevicePlatformType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "fcm_device_tokens",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "device_token"})
    })
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class FcmDeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_token", length = 500, nullable = false)
    private String deviceToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_platform", length = 20, nullable = false)
    private DevicePlatformType devicePlatform;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_notification_sent_at")
    private LocalDateTime lastNotificationSentAt;

    public static FcmDeviceToken create(User user, String deviceToken, DevicePlatformType devicePlatform) {
        return FcmDeviceToken.builder()
            .user(user)
            .deviceToken(deviceToken)
            .devicePlatform(devicePlatform)
            .build();
    }

    public void updateDeviceToken(String newDeviceToken, DevicePlatformType newDevicePlatform) {
        this.deviceToken = newDeviceToken;
        this.devicePlatform = newDevicePlatform;
    }

    public void updateLastNotificationSentAt() {
        this.lastNotificationSentAt = LocalDateTime.now();
    }

}
