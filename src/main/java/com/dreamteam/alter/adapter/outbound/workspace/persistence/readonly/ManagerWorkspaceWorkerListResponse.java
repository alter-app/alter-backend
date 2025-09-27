package com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly;

import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerStatus;
import com.dreamteam.alter.domain.workspace.type.WorkerPositionType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerWorkspaceWorkerListResponse {

    private Long id;

    private WorkspaceWorkerUserResponse user;

    @Enumerated(EnumType.STRING)
    private WorkspaceWorkerStatus status;

    @Enumerated(EnumType.STRING)
    private WorkerPositionType position;

    private LocalDate employedAt;

    private LocalDate resignedAt;

    private LocalDateTime createdAt;

    /**
     * WorkspaceWorker 엔티티로부터 DTO 생성
     */
    public static ManagerWorkspaceWorkerListResponse from(WorkspaceWorker workspaceWorker) {
        return new ManagerWorkspaceWorkerListResponse(
            workspaceWorker.getId(),
            WorkspaceWorkerUserResponse.from(workspaceWorker.getUser()),
            workspaceWorker.getStatus(),
            WorkerPositionType.WORKER,
            workspaceWorker.getEmployedAt(),
            workspaceWorker.getResignedAt(),
            workspaceWorker.getCreatedAt()
        );
    }

    /**
     * ManagerUser 엔티티로부터 DTO 생성 (점주용)
     */
    public static ManagerWorkspaceWorkerListResponse from(ManagerUser managerUser) {
        return new ManagerWorkspaceWorkerListResponse(
            managerUser.getId(),
            WorkspaceWorkerUserResponse.from(managerUser.getUser()),
            WorkspaceWorkerStatus.ACTIVATED, // 점주는 항상 활성 상태
            WorkerPositionType.OWNER, // TODO: 업장 매니저에 대한 직책 추가
            null, // 점주는 채용일 없음
            null, // 점주는 퇴사일 없음
            managerUser.getCreatedAt()
        );
    }

}
