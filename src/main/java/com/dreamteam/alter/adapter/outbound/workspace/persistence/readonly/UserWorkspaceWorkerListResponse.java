package com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly;

import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
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
public class UserWorkspaceWorkerListResponse {

    private Long id;

    private WorkspaceWorkerUserResponse user;

    @Enumerated(EnumType.STRING)
    private WorkerPositionType position;

    private LocalDate employedAt;

    private LocalDateTime createdAt;

    /**
     * WorkspaceWorker 엔티티로부터 DTO 생성
     */
    public static UserWorkspaceWorkerListResponse from(WorkspaceWorker workspaceWorker) {
        return new UserWorkspaceWorkerListResponse(
            workspaceWorker.getId(),
            WorkspaceWorkerUserResponse.from(workspaceWorker.getUser()),
            WorkerPositionType.WORKER,
            workspaceWorker.getEmployedAt(),
            workspaceWorker.getCreatedAt()
        );
    }

    /**
     * ManagerUser 엔티티로부터 DTO 생성 (점주용)
     */
    public static UserWorkspaceWorkerListResponse from(ManagerUser managerUser) {
        return new UserWorkspaceWorkerListResponse(
            managerUser.getId(),
            WorkspaceWorkerUserResponse.from(managerUser.getUser()),
            WorkerPositionType.OWNER,
            null, // 점주는 채용일 없음
            managerUser.getCreatedAt()
        );
    }

}
