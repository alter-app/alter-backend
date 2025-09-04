package com.dreamteam.alter.domain.user.entity;

import com.dreamteam.alter.domain.user.type.SocialProvider;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "user_socials")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class UserSocial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    public static UserSocial create(User user, SocialProvider provider, String socialId, String refreshToken) {
        return UserSocial.builder()
            .user(user)
            .socialProvider(provider)
            .socialId(socialId)
            .refreshToken(refreshToken)
            .build();
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
