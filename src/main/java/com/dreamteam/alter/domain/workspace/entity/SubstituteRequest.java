package com.dreamteam.alter.domain.workspace.entity;

import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestType;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "substitute_requests")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class SubstituteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "workspace_shift_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private WorkspaceShift workspaceShift;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private SubstituteRequestType requestType;

    @OneToMany(mappedBy = "substituteRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SubstituteRequestTarget> targets;

    @Column(name = "accepted_worker_id")
    private Long acceptedWorkerId;

    @Column(name = "approver_id")
    private Long approverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubstituteRequestStatus status;

    @Column(name = "request_reason")
    private String requestReason;

    @Column(name = "target_rejection_reason")
    private String targetRejectionReason;

    @Column(name = "approver_rejection_reason")
    private String approverRejectionReason;

    @Column(name = "approval_comment")
    private String approvalComment;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static SubstituteRequest create(
        WorkspaceShift workspaceShift,
        Long requesterId,
        SubstituteRequestType requestType,
        String requestReason
    ) {
        return SubstituteRequest.builder()
            .workspaceShift(workspaceShift)
            .requesterId(requesterId)
            .requestType(requestType)
            .requestReason(requestReason)
            .status(SubstituteRequestStatus.PENDING)
            .expiresAt(LocalDateTime.now().plusDays(7))
            .targets(new ArrayList<>())
            .build();
    }

    public void accept(Long workerId) {
        this.acceptedWorkerId = workerId;
        this.status = SubstituteRequestStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
        
        // ALL 타입인 경우 해당 target의 상태도 업데이트
        if (this.requestType == SubstituteRequestType.ALL && this.targets != null && !this.targets.isEmpty()) {
            // 수락한 사용자의 상태를 ACCEPTED로 변경
            this.targets.stream()
                .filter(target -> target.getTargetWorkerId().equals(workerId))
                .findFirst()
                .ifPresent(SubstituteRequestTarget::accept);
            
            // 나머지 PENDING 상태인 대상자들을 ACCEPTED_BY_OTHERS로 변경
            this.targets.stream()
                .filter(target -> !target.getTargetWorkerId().equals(workerId))
                .filter(target -> target.getStatus().name().equals("PENDING"))
                .forEach(SubstituteRequestTarget::markAsAcceptedByOthers);
        }
    }

    public void rejectByTarget(String rejectionReason) {
        this.targetRejectionReason = rejectionReason;
        this.status = SubstituteRequestStatus.REJECTED_BY_TARGET;
        this.processedAt = LocalDateTime.now();
    }

    public void rejectByTarget(Long workerId, String rejectionReason) {
        // ALL 타입인 경우 해당 target의 상태 업데이트
        if (this.requestType == SubstituteRequestType.ALL && this.targets != null && !this.targets.isEmpty()) {
            this.targets.stream()
                .filter(target -> target.getTargetWorkerId().equals(workerId))
                .findFirst()
                .ifPresent(target -> target.reject(rejectionReason));
        } else {
            // SPECIFIC 타입인 경우 기존 로직
            rejectByTarget(rejectionReason);
        }
    }

    public void approve(Long approverId, String approvalComment) {
        this.approverId = approverId;
        this.approvalComment = approvalComment;
        this.status = SubstituteRequestStatus.APPROVED;
        this.processedAt = LocalDateTime.now();

        // 수락한 사용자의 SubstituteRequestTarget 상태도 APPROVED로 변경
        if (this.targets != null && !this.targets.isEmpty() && this.acceptedWorkerId != null) {
            this.targets.stream()
                .filter(target -> target.getTargetWorkerId().equals(this.acceptedWorkerId))
                .findFirst()
                .ifPresent(SubstituteRequestTarget::approve);
        }
    }

    public void rejectByApprover(Long approverId, String rejectionReason) {
        this.approverId = approverId;
        this.approverRejectionReason = rejectionReason;
        this.status = SubstituteRequestStatus.REJECTED_BY_APPROVER;
        this.processedAt = LocalDateTime.now();

        // 모든 SubstituteRequestTarget의 상태도 REJECTED_BY_APPROVER로 변경
        if (this.targets != null && !this.targets.isEmpty()) {
            this.targets.forEach(target -> target.rejectByApprover(rejectionReason));
        }
    }

    public void cancel() {
        this.status = SubstituteRequestStatus.CANCELLED;
        this.processedAt = LocalDateTime.now();

        // 모든 SubstituteRequestTarget의 상태도 CANCELLED로 변경
        if (this.targets != null && !this.targets.isEmpty()) {
            this.targets.forEach(SubstituteRequestTarget::cancel);
        }
    }

    public void expire() {
        this.status = SubstituteRequestStatus.EXPIRED;

        // 모든 SubstituteRequestTarget의 상태도 EXPIRED로 변경
        if (ObjectUtils.isNotEmpty(targets)) {
            this.targets.forEach(SubstituteRequestTarget::expire);
        }
    }

    public boolean canBeCancelledBy(Long userId) {
        return this.requesterId.equals(userId)
            && (this.status == SubstituteRequestStatus.PENDING
                || this.status == SubstituteRequestStatus.ACCEPTED);
    }

    public boolean canBeAcceptedBy(Long workerId) {
        if (this.status != SubstituteRequestStatus.PENDING) {
            return false;
        }
        
        // 만료된 요청은 수락 불가
        if (this.expiresAt != null && this.expiresAt.isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // targets에서 해당 worker가 PENDING 상태인지 확인
        return this.targets != null && !this.targets.isEmpty() && this.targets.stream()
            .anyMatch(target -> target.getTargetWorkerId().equals(workerId) 
                && target.getStatus().name().equals("PENDING"));
    }

    public boolean canBeRejectedBy(Long workerId) {
        if (this.status != SubstituteRequestStatus.PENDING) {
            return false;
        }
        
        // 만료된 요청은 거절 불가
        if (this.expiresAt != null && this.expiresAt.isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // targets에서 해당 worker가 PENDING 상태인지 확인
        return this.targets != null && !this.targets.isEmpty() && this.targets.stream()
            .anyMatch(target -> target.getTargetWorkerId().equals(workerId) 
                && target.getStatus().name().equals("PENDING"));
    }

}

