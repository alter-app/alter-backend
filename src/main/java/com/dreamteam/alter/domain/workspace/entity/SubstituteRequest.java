package com.dreamteam.alter.domain.workspace.entity;

import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestType;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestTargetStatus;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Entity
@Getter
@Table(name = "substitute_requests")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class SubstituteRequest {

    private static final int DEFAULT_EXPIRY_DAYS = 7;

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

    @Version
    @Column(name = "version")
    private Long version;

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
            .expiresAt(LocalDateTime.now().plusDays(DEFAULT_EXPIRY_DAYS))
            .targets(new ArrayList<>())
            .build();
    }

    public void accept(Long workerId) {
        this.acceptedWorkerId = workerId;
        this.status = SubstituteRequestStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
        
        // ALL 타입인 경우: 전체 PENDING 타깃을 먼저 ACCEPTED_BY_OTHERS로 변경 후, 수락자만 ACCEPTED로 변경
        if (SubstituteRequestType.ALL.equals(this.requestType)) {
            // 1단계: 모든 PENDING 타깃을 ACCEPTED_BY_OTHERS로 변경
            updateAllPendingTargetsStatus(SubstituteRequestTarget::markAsAcceptedByOthers);
            
            // 2단계: 수락한 사용자만 ACCEPTED로 변경
            updateTargetStatus(workerId, SubstituteRequestTarget::accept);
        } else {
            // SPECIFIC 타입인 경우: 해당 사용자만 ACCEPTED로 변경
            updateTargetStatus(workerId, SubstituteRequestTarget::accept);
        }
    }

    public void rejectByTarget(Long workerId, String rejectionReason) {
        updateTargetStatus(workerId, target -> target.reject(rejectionReason));
        
        // 모든 대상자가 거절했는지 확인
        if (areAllTargetsRejected()) {
            this.status = SubstituteRequestStatus.REJECTED_BY_TARGET;
            this.processedAt = LocalDateTime.now();
        }
    }

    public void approve(Long approverId, String approvalComment) {
        this.approverId = approverId;
        this.approvalComment = approvalComment;
        this.status = SubstituteRequestStatus.APPROVED;
        this.processedAt = LocalDateTime.now();

        // 수락한 사용자의 SubstituteRequestTarget 상태도 APPROVED로 변경
        if (ObjectUtils.isNotEmpty(this.acceptedWorkerId)) {
            updateTargetStatus(this.acceptedWorkerId, SubstituteRequestTarget::approve);
        }
    }

    public void rejectByApprover(Long approverId, String rejectionReason) {
        this.approverId = approverId;
        this.approverRejectionReason = rejectionReason;
        this.status = SubstituteRequestStatus.REJECTED_BY_APPROVER;
        this.processedAt = LocalDateTime.now();

        // 모든 SubstituteRequestTarget의 상태도 REJECTED_BY_APPROVER로 변경
        this.targets.forEach(target -> target.rejectByApprover(rejectionReason));
    }

    public void cancel() {
        this.status = SubstituteRequestStatus.CANCELLED;
        this.processedAt = LocalDateTime.now();

        // 모든 SubstituteRequestTarget의 상태도 CANCELLED로 변경
        this.targets.forEach(SubstituteRequestTarget::cancel);
    }

    public void expire() {
        this.status = SubstituteRequestStatus.EXPIRED;
        this.processedAt = LocalDateTime.now();

        // 모든 SubstituteRequestTarget의 상태도 EXPIRED로 변경
        if (ObjectUtils.isNotEmpty(targets)) {
            this.targets.forEach(SubstituteRequestTarget::expire);
        }
    }

    public boolean canBeCancelledBy(Long userId) {
        return this.requesterId.equals(userId)
            && (SubstituteRequestStatus.PENDING.equals(this.status) || SubstituteRequestStatus.ACCEPTED.equals(this.status));
    }

    public boolean canBeAcceptedBy(Long workerId) {
        if (!SubstituteRequestStatus.PENDING.equals(this.status)) {
            return false;
        }
        
        // 만료된 요청은 수락 불가
        if (this.expiresAt.isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // targets에서 해당 worker가 PENDING 상태인지 확인
        return this.targets.stream()
            .anyMatch(target -> target.getTargetWorkerId().equals(workerId) 
                && SubstituteRequestTargetStatus.PENDING.equals(target.getStatus()));
    }

    public boolean canBeRejectedBy(Long workerId) {
        if (!SubstituteRequestStatus.PENDING.equals(this.status)) {
            return false;
        }
        
        // 만료된 요청은 거절 불가
        if (this.expiresAt.isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // targets에서 해당 worker가 PENDING 상태인지 확인
        return this.targets.stream()
            .anyMatch(target -> target.getTargetWorkerId().equals(workerId) 
                && SubstituteRequestTargetStatus.PENDING.equals(target.getStatus()));
    }

    /**
     * 특정 workerId에 해당하는 타깃의 상태를 업데이트합니다.
     */
    private void updateTargetStatus(Long workerId, Consumer<SubstituteRequestTarget> action) {
        this.targets.stream()
            .filter(target -> target.getTargetWorkerId().equals(workerId))
            .findFirst()
            .ifPresent(action);
    }

    /**
     * 모든 PENDING 상태인 타깃들에게 일괄적으로 상태 변경 액션을 적용합니다.
     */
    private void updateAllPendingTargetsStatus(Consumer<SubstituteRequestTarget> action) {
        this.targets.stream()
            .filter(target -> SubstituteRequestTargetStatus.PENDING.equals(target.getStatus()))
            .forEach(action);
    }

    /**
     * 모든 대상자가 거절했는지 확인합니다.
     */
    private boolean areAllTargetsRejected() {        
        return this.targets.stream()
            .allMatch(target -> SubstituteRequestTargetStatus.REJECTED.equals(target.getStatus()));
    }

}
