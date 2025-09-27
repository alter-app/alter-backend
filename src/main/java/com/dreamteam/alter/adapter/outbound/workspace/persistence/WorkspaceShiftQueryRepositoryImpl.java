package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.dreamteam.alter.domain.workspace.entity.QWorkspaceShift.workspaceShift;

@Repository
@RequiredArgsConstructor
public class WorkspaceShiftQueryRepositoryImpl implements WorkspaceShiftQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<WorkspaceShift> findByUserAndDateRange(User user, int year, int month) {
        return queryFactory
            .selectFrom(workspaceShift)
            .where(workspaceShift.assignedWorkspaceWorker.user.eq(user)
                .and(workspaceShift.startDateTime.year().eq(year))
                .and(workspaceShift.startDateTime.month().eq(month))
                .and(workspaceShift.status.ne(WorkspaceShiftStatus.DELETED)))
            .orderBy(workspaceShift.startDateTime.asc())
            .fetch();
    }

    @Override
    public List<WorkspaceShift> findByWorkspaceAndDateRange(Workspace workspace, int year, int month) {
        return queryFactory
            .selectFrom(workspaceShift)
            .where(workspaceShift.workspace.eq(workspace)
                .and(workspaceShift.startDateTime.year().eq(year))
                .and(workspaceShift.startDateTime.month().eq(month))
                .and(workspaceShift.status.eq(WorkspaceShiftStatus.CONFIRMED)))
            .orderBy(workspaceShift.startDateTime.asc())
            .fetch();
    }

    @Override
    public List<WorkspaceShift> findByManagerAndDateRange(
        ManagerUser managerUser,
        Long workspaceId,
        int year,
        int month
    ) {
        return queryFactory
            .selectFrom(workspaceShift)
            .where(workspaceShift.workspace.id.eq(workspaceId)
                .and(workspaceShift.workspace.managerUser.eq(managerUser))
                .and(workspaceShift.startDateTime.year().eq(year))
                .and(workspaceShift.startDateTime.month().eq(month))
                .and(workspaceShift.status.ne(WorkspaceShiftStatus.DELETED)))
            .orderBy(workspaceShift.startDateTime.asc())
            .fetch();
    }

    @Override
    public Optional<WorkspaceShift> findById(Long id) {
        WorkspaceShift result = queryFactory
            .selectFrom(workspaceShift)
            .where(workspaceShift.id.eq(id)
                .and(workspaceShift.status.ne(WorkspaceShiftStatus.DELETED)))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public boolean hasConflictingSchedule(WorkspaceWorker workspaceWorker, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Long count = queryFactory
            .select(workspaceShift.count())
            .from(workspaceShift)
            .where(workspaceShift.assignedWorkspaceWorker.eq(workspaceWorker)
                .and(workspaceShift.status.eq(WorkspaceShiftStatus.CONFIRMED))
                .and(
                    // 시간 겹침 확인: 새로운 스케줄이 기존 스케줄과 겹치는지 확인
                    workspaceShift.startDateTime.lt(endDateTime)
                        .and(workspaceShift.endDateTime.gt(startDateTime))
                ))
            .fetchOne();

        return count != null && count > 0;
    }
}
