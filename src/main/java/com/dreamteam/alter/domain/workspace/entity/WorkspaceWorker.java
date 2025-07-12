package com.dreamteam.alter.domain.workspace.entity;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "workspace_workers")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class WorkspaceWorker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkspaceWorkerStatus status;

    @CreatedDate
    @Column(name = "employed_at", nullable = false, updatable = false)
    private LocalDateTime employedAt;

    @Column(name = "resigned_at")
    private LocalDateTime resignedAt;

    public static WorkspaceWorker create(
        Workspace workspace,
        User user
    ) {
        return WorkspaceWorker.builder()
            .workspace(workspace)
            .user(user)
            .status(WorkspaceWorkerStatus.ACTIVATED)
            .build();
    }

    public void resign() {
        this.status = WorkspaceWorkerStatus.RESIGNED;
        this.resignedAt = LocalDateTime.now();
    }

}
