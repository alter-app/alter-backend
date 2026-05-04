package com.dreamteam.alter.domain.user.entity;

import com.dreamteam.alter.domain.user.type.UserGender;
import com.dreamteam.alter.domain.user.type.UserRole;
import com.dreamteam.alter.domain.user.type.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Column(name = "email", length = 255, nullable = true, unique = true)
    private String email;

    @Column(name = "password", length = 255, nullable = true)
    private String password;

    @Column(name = "name", length = 12, nullable = false)
    private String name;

    @Column(name = "nickname", length = 64, nullable = false, unique = true)
    private String nickname;

    @Column(name = "contact", length = 13, nullable = false)
    private String contact;

    @Column(name = "birthday", length = 8, nullable = false)
    private String birthday;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private UserGender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

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
    @Builder.Default
    private List<UserCertificate> certificates = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserSocial> userSocials = new ArrayList<>();

    public static User create(
        String contact,
        String encodedPassword,
        String name,
        String nickname,
        UserGender gender,
        String birthday,
        String email
    ) {
        return User.builder()
            .email(email)
            .password(encodedPassword)
            .name(name)
            .nickname(nickname)
            .contact(contact)
            .birthday(birthday)
            .gender(gender)
            .role(UserRole.ROLE_USER)
            .status(UserStatus.ACTIVE)
            .build();
    }

    public static User createWithSocial(
        String contact,
        String name,
        String nickname,
        UserGender gender,
        String birthday,
        String email
    ) {
        return User.builder()
            .email(email)
            .password(null)
            .name(name)
            .nickname(nickname)
            .contact(contact)
            .birthday(birthday)
            .gender(gender)
            .role(UserRole.ROLE_USER)
            .status(UserStatus.ACTIVE)
            .build();
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void removeEmail() {
        this.email = null;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void addCertificate(UserCertificate userCertificate) {
        certificates.add(userCertificate);
    }

    public void addUserSocial(UserSocial userSocial) {
        userSocials.add(userSocial);
    }

    /**
     * 비밀번호를 업데이트합니다.
     *
     * @param encodedPassword 암호화된 비밀번호
     */
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /**
     * 회원 상태를 변경합니다.
     *
     * @param newStatus 변경할 상태
     */
    public void updateStatus(UserStatus newStatus) {
        if (this.status.equals(newStatus)) {
            throw new IllegalArgumentException("이미 동일한 상태입니다.");
        }
        this.status = newStatus;
    }

    public void withdraw() {
        String anonymize = id + "_탈퇴";
        name = anonymize;
        nickname = anonymize;
        contact = anonymize;
        email = null;
        status = UserStatus.DELETED;
    }
}
