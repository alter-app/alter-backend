package com.dreamteam.alter.domain.notification.entity;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.type.NotificationType;
import com.dreamteam.alter.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "target_user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User targetUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", length = 20, nullable = false)
    private TokenScope scope;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    private NotificationType type;

    @Column(name = "device_token", length = 500)
    private String deviceToken;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "body", length = 1000, nullable = false)
    private String body;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public void markAsRead() {
        this.isRead = true;
    }

    public static Notification create(
        User targetUser,
        TokenScope scope,
        NotificationType type,
        String deviceToken,
        String title,
        String body
    ) {
        Objects.requireNonNull(type, "notification type must not be null");
        return Notification.builder()
            .targetUser(targetUser)
            .scope(scope)
            .type(type)
            .deviceToken(deviceToken)
            .title(title)
            .body(body)
            .build();
    }
}
