package com.dreamteam.alter.domain.auth.entity;

import com.dreamteam.alter.domain.auth.type.AuthorizationStatus;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "authorizations")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Authorization {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", length = 20, nullable = false)
    private TokenScope scope;

    @Column(name = "access_token", length = Integer.MAX_VALUE, nullable = false)
    private String accessToken;

    @Column(name = "access_token_expired_at", nullable = false)
    private Instant accessTokenExpiredAt;

    @Column(name = "refresh_token", length = Integer.MAX_VALUE, nullable = false)
    private String refreshToken;

    @Column(name = "refresh_token_expired_at", nullable = false)
    private Instant refreshTokenExpiredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private AuthorizationStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Authorization create(
        String id,
        User user,
        TokenScope scope,
        String accessToken,
        Instant accessTokenExpiredAt,
        String refreshToken,
        Instant refreshTokenExpiredAt
    ) {
        return Authorization.builder()
            .id(id)
            .user(user)
            .scope(scope)
            .accessToken(accessToken)
            .accessTokenExpiredAt(accessTokenExpiredAt)
            .refreshToken(refreshToken)
            .refreshTokenExpiredAt(refreshTokenExpiredAt)
            .status(AuthorizationStatus.ACTIVE)
            .build();
    }

    public void expire() {
        this.status = AuthorizationStatus.EXPIRED;
    }

}
