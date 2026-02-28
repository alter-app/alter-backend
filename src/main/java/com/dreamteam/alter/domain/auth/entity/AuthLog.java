package com.dreamteam.alter.domain.auth.entity;

import com.dreamteam.alter.domain.auth.type.AuthLogType;
import com.dreamteam.alter.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "auth_logs")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class AuthLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "authorization_id", nullable = false)
    private Authorization authorization;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private AuthLogType type;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static AuthLog create(User user, Authorization authorization, AuthLogType type) {
        return AuthLog.builder()
            .user(user)
            .authorization(authorization)
            .type(type)
            .build();
    }
}
