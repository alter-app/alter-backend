package com.dreamteam.alter.domain.notification.entity;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

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
    @Column(name = "scope", nullable = false)
    private TokenScope scope;

    @Column(name = "device_token", length = 500, nullable = false)
    private String deviceToken;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "body", length = 1000, nullable = false)
    private String body;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static Notification create(
        User targetUser,
        TokenScope scope,
        String deviceToken,
        String title,
        String body
    ) {
        return Notification.builder()
            .targetUser(targetUser)
            .scope(scope)
            .deviceToken(deviceToken)
            .title(title)
            .body(body)
            .build();
    }
}
