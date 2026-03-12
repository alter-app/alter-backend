package com.dreamteam.alter.domain.workspace.entity;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.type.BusinessJoinRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "business_join_requests")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class BusinessJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BusinessJoinRequestStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static BusinessJoinRequest create(Workspace workspace, User user) {
        return BusinessJoinRequest.builder()
            .workspace(workspace)
            .user(user)
            .status(BusinessJoinRequestStatus.PENDING)
            .build();
    }

    public void approve() {
        if (!BusinessJoinRequestStatus.PENDING.equals(this.status)) {
            throw new CustomException(ErrorCode.CONFLICT, "승인할 수 없는 상태의 합류 요청입니다.");
        }
        this.status = BusinessJoinRequestStatus.APPROVED;
    }

    public void reject() {
        if (!BusinessJoinRequestStatus.PENDING.equals(this.status)) {
            throw new CustomException(ErrorCode.CONFLICT, "거절할 수 없는 상태의 합류 요청입니다.");
        }
        this.status = BusinessJoinRequestStatus.REJECTED;
    }
}
