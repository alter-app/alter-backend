package com.dreamteam.alter.domain.workspace.entity;

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
    @Column(name = "status", nullable = false)
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
        return WorkspaceShift.builder()
            .workspace(workspace)
            .startDateTime(startDateTime)
            .endDateTime(endDateTime)
            .position(position)
            .status(status)
            .build();
    }

    public void assignWorker(WorkspaceWorker workspaceWorker) {
        this.assignedWorkspaceWorker = workspaceWorker;
        this.status = WorkspaceShiftStatus.CONFIRMED;
    }

    public void unassignWorker() {
        this.assignedWorkspaceWorker = null;
        this.status = WorkspaceShiftStatus.CANCELLED;
    }

    public void update(LocalDateTime startDateTime, LocalDateTime endDateTime, String position) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.position = position;
    }

    public void delete() {
        this.status = WorkspaceShiftStatus.DELETED;
    }

}
