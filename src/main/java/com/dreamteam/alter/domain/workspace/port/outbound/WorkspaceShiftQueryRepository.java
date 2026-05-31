package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.model.WorkspaceShiftTodayResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkspaceShiftQueryRepository {
    List<WorkspaceShiftTodayResponse> getTodayShiftList(Long workspaceId);

    List<WorkspaceShift> findByUserAndDateRange(User user, int year, int month);
    List<WorkspaceShift> findByUserAndWeeklyRange(User user, LocalDate startDate, LocalDate endDate);
    List<WorkspaceShift> findByUserAndDate(User user, int year, int month, int day);
    List<WorkspaceShift> findByWorkspaceAndDateRange(Workspace workspace, int year, int month);
    List<WorkspaceShift> findByManagerAndDateRange(ManagerUser managerUser, Long workspaceId, int year, int month);
    Optional<WorkspaceShift> findById(Long id);
    boolean hasConflictingSchedule(WorkspaceWorker workspaceWorker, LocalDateTime startDateTime, LocalDateTime endDateTime);
    List<WorkspaceShift> findConfirmedByWorkerIdsAndDateRange(List<Long> workerIds, LocalDateTime startDateTime, LocalDateTime endDateTime);
    List<WorkspaceShift> findByUserAndWorkspaceAndMonthFrom(User user, Workspace workspace, int year, int month, LocalDateTime fromInclusive);
    List<WorkspaceShift> findFutureShiftsByAssignedWorker(WorkspaceWorker worker, LocalDateTime fromInclusive);
}
