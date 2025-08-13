package com.dreamteam.alter.domain.reputation.entity;

import com.dreamteam.alter.domain.reputation.type.ReputationRequestStatus;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestType;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "reputation_requests")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class ReputationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "workspace_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Workspace workspace;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", length = 20, nullable = false)
    private ReputationRequestType requestType;

    @Column(name = "requester_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ReputationType requesterType;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "target_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ReputationType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ReputationRequestStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expired_at", nullable = false, updatable = false)
    private LocalDateTime expiredAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static ReputationRequest create(
        Workspace workspace, // nullable
        ReputationType requesterType,
        Long requesterId,
        ReputationRequestType requestType,
        ReputationType targetType,
        Long targetId
    ) {
        return ReputationRequest.builder()
            .workspace(workspace)
            .requestType(requestType)
            .requesterType(requesterType)
            .requesterId(requesterId)
            .targetType(targetType)
            .targetId(targetId)
            .status(ReputationRequestStatus.REQUESTED)
            .expiredAt(LocalDateTime.now().plusDays(7))
            .build();
    }

    public void expire() {
        this.status = ReputationRequestStatus.EXPIRED;
    }

}
