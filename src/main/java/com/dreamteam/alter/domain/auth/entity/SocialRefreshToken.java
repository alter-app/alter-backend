package com.dreamteam.alter.domain.auth.entity;

import com.dreamteam.alter.domain.user.type.SocialProvider;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "social_refresh_tokens")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class SocialRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", length = 20, nullable = false)
    private SocialProvider socialProvider;

    @Column(name = "social_id", nullable = false, length = Integer.MAX_VALUE, unique = true)
    private String socialId;

    @Column(name = "refresh_token", length = Integer.MAX_VALUE, nullable = false)
    private String refreshToken;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static SocialRefreshToken create(SocialProvider provider, String socialId, String refreshToken) {
        return SocialRefreshToken.builder()
            .socialProvider(provider)
            .socialId(socialId)
            .refreshToken(refreshToken)
            .build();
    }

    public void update(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}
