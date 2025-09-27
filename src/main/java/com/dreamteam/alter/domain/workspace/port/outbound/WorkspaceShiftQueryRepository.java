package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkspaceShiftQueryRepository {
    List<WorkspaceShift> findByUserAndDateRange(User user, int year, int month);
    List<WorkspaceShift> findByWorkspaceAndDateRange(Workspace workspace, int year, int month);
    List<WorkspaceShift> findByManagerAndDateRange(ManagerUser managerUser, Long workspaceId, int year, int month);
    Optional<WorkspaceShift> findById(Long id);
    
    /**
     * 특정 근무자가 이미 배정된 스케줄이 있는지 확인
     * @param workspaceWorker 확인할 근무자
     * @param startDateTime 스케줄 시작 시간
     * @param endDateTime 스케줄 종료 시간
     * @return 이미 배정된 스케줄이 있으면 true
     */
    boolean hasConflictingSchedule(WorkspaceWorker workspaceWorker, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
