package com.dreamteam.alter.domain.workspace.entity;

import com.dreamteam.alter.domain.workspace.type.SubstituteRequestTargetStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "substitute_request_targets")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class SubstituteRequestTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "substitute_request_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private SubstituteRequest substituteRequest;

    @Column(name = "target_worker_id", nullable = false)
    private Long targetWorkerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubstituteRequestTargetStatus status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static SubstituteRequestTarget create(SubstituteRequest substituteRequest, Long targetWorkerId) {
        return SubstituteRequestTarget.builder()
            .substituteRequest(substituteRequest)
            .targetWorkerId(targetWorkerId)
            .status(SubstituteRequestTargetStatus.PENDING)
            .build();
    }

    public void accept() {
        this.status = SubstituteRequestTargetStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject(String rejectionReason) {
        this.status = SubstituteRequestTargetStatus.REJECTED;
        this.rejectionReason = rejectionReason;
        this.respondedAt = LocalDateTime.now();
    }

    public void markAsAcceptedByOthers() {
        this.status = SubstituteRequestTargetStatus.ACCEPTED_BY_OTHERS;
        this.respondedAt = LocalDateTime.now();
    }

    public void approve() {
        this.status = SubstituteRequestTargetStatus.APPROVED;
        this.respondedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = SubstituteRequestTargetStatus.CANCELLED;
        this.respondedAt = LocalDateTime.now();
    }

    public void rejectByApprover(String rejectionReason) {
        this.status = SubstituteRequestTargetStatus.REJECTED_BY_APPROVER;
        this.rejectionReason = rejectionReason;
        this.respondedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = SubstituteRequestTargetStatus.EXPIRED;
    }

}
