package com.dreamteam.alter.domain.workspace.entity;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.type.BusinessInvitationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "business_invitations")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class BusinessInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User invitedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by", nullable = false)
    private ManagerUser invitedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private BusinessInvitationStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static BusinessInvitation create(Workspace workspace, User invitedUser, ManagerUser invitedBy) {
        return BusinessInvitation.builder()
            .workspace(workspace)
            .invitedUser(invitedUser)
            .invitedBy(invitedBy)
            .status(BusinessInvitationStatus.PENDING)
            .expiresAt(LocalDateTime.now().plusDays(7))
            .build();
    }

    public void accept() {
        if (!BusinessInvitationStatus.PENDING.equals(this.status) || isExpired()) {
            throw new CustomException(ErrorCode.CONFLICT, "수락할 수 없는 상태의 초대입니다.");
        }
        this.status = BusinessInvitationStatus.ACCEPTED;
    }

    public void decline() {
        if (!BusinessInvitationStatus.PENDING.equals(this.status) || isExpired()) {
            throw new CustomException(ErrorCode.CONFLICT, "거절할 수 없는 상태의 초대입니다.");
        }
        this.status = BusinessInvitationStatus.DECLINED;
    }

    public void expire() {
        this.status = BusinessInvitationStatus.EXPIRED;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
