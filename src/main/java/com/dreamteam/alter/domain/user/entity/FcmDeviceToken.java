package com.dreamteam.alter.domain.user.entity;

import com.dreamteam.alter.domain.user.type.DevicePlatformType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "fcm_device_tokens")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_notification_sent_at")
    private LocalDateTime lastNotificationSentAt;

    public static FcmDeviceToken create(User user, String deviceToken, DevicePlatformType devicePlatform) {
        return FcmDeviceToken.builder()
            .user(user)
            .deviceToken(deviceToken)
            .devicePlatform(devicePlatform)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    public void updateDeviceToken(String newDeviceToken, DevicePlatformType newDevicePlatform) {
        this.deviceToken = newDeviceToken;
        this.devicePlatform = newDevicePlatform;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLastNotificationSentAt() {
        this.lastNotificationSentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

}
