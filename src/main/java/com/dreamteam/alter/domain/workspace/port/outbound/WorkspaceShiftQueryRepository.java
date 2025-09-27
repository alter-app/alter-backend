package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkspaceShiftQueryRepository {
    List<WorkspaceShift> findByUserAndDateRange(User user, int year, int month);
    List<WorkspaceShift> findByUserAndWeeklyRange(User user, LocalDate startDate, LocalDate endDate);
    List<WorkspaceShift> findByWorkspaceAndDateRange(Workspace workspace, int year, int month);
    List<WorkspaceShift> findByManagerAndDateRange(ManagerUser managerUser, Long workspaceId, int year, int month);
    Optional<WorkspaceShift> findById(Long id);
    boolean hasConflictingSchedule(WorkspaceWorker workspaceWorker, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
