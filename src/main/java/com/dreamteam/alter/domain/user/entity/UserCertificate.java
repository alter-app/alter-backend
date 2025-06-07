package com.dreamteam.alter.domain.user.entity;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserCertificateRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UpdateUserCertificateRequestDto;
import com.dreamteam.alter.domain.user.type.CertificateType;
import com.dreamteam.alter.domain.user.type.UserCertificateStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "user_certificates")
@DynamicUpdate
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class UserCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CertificateType type;

    @Column(name = "certificate_name", length = 255, nullable = false)
    private String certificateName;

    @Column(name = "certificate_id", length = Integer.MAX_VALUE, nullable = true)
    private String certificateId;

    @Column(name = "publisher_name", length = 255, nullable = false)
    private String publisherName;

    @Column(name = "issued_at", nullable = false)
    private LocalDate issuedAt;

    @Column(name = "expires_at", nullable = true)
    private LocalDate expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private UserCertificateStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static UserCertificate create(CreateUserCertificateRequestDto request, User user) {
        return UserCertificate.builder()
            .user(user)
            .type(request.getType())
            .certificateName(request.getCertificateName())
            .certificateId(request.getCertificateId())
            .publisherName(request.getPublisherName())
            .issuedAt(request.getIssuedAt())
            .expiresAt(request.getExpiresAt())
            .status(UserCertificateStatus.ACTIVATED)
            .build();
    }

    public void update(UpdateUserCertificateRequestDto request) {
        this.type = request.getType();
        this.certificateName = request.getCertificateName();
        this.certificateId = request.getCertificateId();
        this.publisherName = request.getPublisherName();
        this.issuedAt = request.getIssuedAt();
        this.expiresAt = request.getExpiresAt();
    }

    public void delete() {
        this.status = UserCertificateStatus.DELETED;
    }

}
