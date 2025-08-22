package com.dreamteam.alter.domain.reputation.entity;

import com.dreamteam.alter.domain.reputation.type.ReputationRequestStatus;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestType;
import com.dreamteam.alter.domain.reputation.type.ReputationStatus;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "expires_at", nullable = false, updatable = false)
    private LocalDateTime expiresAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "reputationRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Reputation> reputations;

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
            .expiresAt(LocalDateTime.now().plusDays(7))
            .build();
    }

    public void expire() {
        this.status = ReputationRequestStatus.EXPIRED;
    }

    public void decline() {
        this.status = ReputationRequestStatus.DECLINED;

        if (ObjectUtils.isNotEmpty(this.reputations)) {
            for (Reputation reputation : this.reputations) {
                if (ReputationStatus.REQUESTED.equals(reputation.getStatus())) {
                    reputation.decline();
                }
            }
        }
    }

    public void accept() {
        this.status = ReputationRequestStatus.COMPLETED;

        if (ObjectUtils.isNotEmpty(this.reputations)) {
            for (Reputation reputation : this.reputations) {
                if (ReputationStatus.REQUESTED.equals(reputation.getStatus())) {
                    reputation.complete();
                }
            }
        }
    }

}
