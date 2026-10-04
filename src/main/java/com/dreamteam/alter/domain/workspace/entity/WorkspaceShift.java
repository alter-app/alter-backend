package com.dreamteam.alter.domain.workspace.entity;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "workspace_shifts")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class WorkspaceShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "workspace_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Workspace workspace;

    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "position", length = 128, nullable = false)
    private String position;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private WorkspaceShiftStatus status;

    @JoinColumn(name = "worker_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private WorkspaceWorker assignedWorkspaceWorker;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static WorkspaceShift create(
        Workspace workspace,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        String position,
        WorkspaceShiftStatus status
    ) {
        validatePeriod(startDateTime, endDateTime);
        return WorkspaceShift.builder()
            .workspace(workspace)
            .startDateTime(startDateTime)
            .endDateTime(endDateTime)
            .position(position)
            .status(status)
            .build();
    }

    public void assignWorker(WorkspaceWorker workspaceWorker) {
        if (status == WorkspaceShiftStatus.DELETED) {
            throw new CustomException(ErrorCode.CONFLICT, "삭제된 근무 일정에는 근무자를 배정할 수 없습니다.");
        }
        this.assignedWorkspaceWorker = workspaceWorker;
        this.status = WorkspaceShiftStatus.CONFIRMED;
    }

    public void unassignWorker() {
        this.assignedWorkspaceWorker = null;
        this.status = WorkspaceShiftStatus.CANCELLED;
    }

    public void update(LocalDateTime startDateTime, LocalDateTime endDateTime, String position) {
        validatePeriod(startDateTime, endDateTime);
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.position = position;
    }

    public void delete() {
        this.status = WorkspaceShiftStatus.DELETED;
    }

    // 역전 구간(start >= end)은 겹침 조건이 절대 참이 되지 않아 겹침 검사를 그대로 통과하므로 생성·수정 모두 막는다
    private static void validatePeriod(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (!startDateTime.isBefore(endDateTime)) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "시작 시간은 종료 시간보다 늦을 수 없습니다.");
        }
    }

}
