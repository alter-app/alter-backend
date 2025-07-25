package com.dreamteam.alter.domain.user.entity;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialUserInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserRequestDto;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import com.dreamteam.alter.domain.user.type.UserGender;
import com.dreamteam.alter.domain.user.type.UserRole;
import com.dreamteam.alter.domain.user.type.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Table(name = "users")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "name", length = 12, nullable = false)
    private String name;

    @Column(name = "nickname", length = 64, nullable = false, unique = true)
    private String nickname;

    @Column(name = "contact", length = 13, nullable = false)
    private String contact;

    @Column(name = "birthday", length = 8, nullable = false)
    private String birthday;

    @Column(name = "gender", nullable = false)
    private UserGender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", length = 12, nullable = false)
    private SocialProvider provider;

    @Column(name = "social_id", length = Integer.MAX_VALUE, nullable = false, unique = true)
    private String socialId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "roles", columnDefinition = "jsonb", nullable = false)
    private List<UserRole> roles;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @SQLRestriction("status != 'DELETED'")
    private UserStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id DESC")
    @SQLRestriction("status != 'DELETED'")
    private List<UserCertificate> certificates;

    public static User create(
        CreateUserRequestDto request,
        SocialUserInfo socialUserInfo
    ) {
        return User.builder()
            .email(socialUserInfo.getEmail())
            .name(request.getName())
            .nickname(request.getNickname())
            .contact(request.getContact())
            .birthday(request.getBirthday())
            .gender(request.getGender())
            .provider(socialUserInfo.getProvider())
            .socialId(socialUserInfo.getSocialId())
            .roles(List.of(UserRole.ROLE_USER))
            .status(UserStatus.ACTIVE)
            .build();
    }

    public void addCertificate(UserCertificate userCertificate) {
        certificates.add(userCertificate);
    }

}
