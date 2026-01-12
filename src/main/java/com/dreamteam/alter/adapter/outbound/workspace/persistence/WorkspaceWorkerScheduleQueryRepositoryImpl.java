package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.QWorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerScheduleStatus;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceWorkerScheduleQueryRepositoryImpl implements WorkspaceWorkerScheduleQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Optional<WorkspaceWorkerSchedule> getByIdWithWorkspaceWorker(Long workerScheduleId) {
		QWorkspaceWorkerSchedule qWorkspaceWorkerSchedule = QWorkspaceWorkerSchedule.workspaceWorkerSchedule;
		QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;

		return Optional.ofNullable(
			queryFactory
				.selectFrom(qWorkspaceWorkerSchedule)
				.join(qWorkspaceWorkerSchedule.workspaceWorker, qWorkspaceWorker).fetchJoin()
				.where(
					qWorkspaceWorkerSchedule.id.eq(workerScheduleId),
					qWorkspaceWorkerSchedule.status.ne(WorkspaceWorkerScheduleStatus.DELETED)
				)
				.fetchOne()
		);
	}

	@Override
	public List<WorkspaceWorkerSchedule> getByWorkspaceWorkerAndDayOfWeekIn(WorkspaceWorker workspaceWorker, List<DayOfWeek> dayOfWeeks) {
		QWorkspaceWorkerSchedule qWorkspaceWorkerSchedule = QWorkspaceWorkerSchedule.workspaceWorkerSchedule;

		return queryFactory
			.selectFrom(qWorkspaceWorkerSchedule)
			.where(
				qWorkspaceWorkerSchedule.workspaceWorker.eq(workspaceWorker),
				qWorkspaceWorkerSchedule.dayOfWeek.in(dayOfWeeks),
				qWorkspaceWorkerSchedule.status.ne(WorkspaceWorkerScheduleStatus.DELETED)
			)
			.fetch();
	}

	@Override
	public boolean existsById(Long workerScheduleId) {
		QWorkspaceWorkerSchedule qWorkspaceWorkerSchedule = QWorkspaceWorkerSchedule.workspaceWorkerSchedule;

		Integer find = queryFactory
			.selectOne()
			.from(qWorkspaceWorkerSchedule)
			.where(
				qWorkspaceWorkerSchedule.id.eq(workerScheduleId),
				qWorkspaceWorkerSchedule.status.ne(WorkspaceWorkerScheduleStatus.DELETED)
			)
			.fetchFirst();

		return find != null;
	}

	@Override
	public void deleteById(Long workerScheduleId) {
		QWorkspaceWorkerSchedule qWorkspaceWorkerSchedule = QWorkspaceWorkerSchedule.workspaceWorkerSchedule;

		queryFactory
			.update(qWorkspaceWorkerSchedule)
			.set(qWorkspaceWorkerSchedule.status, WorkspaceWorkerScheduleStatus.DELETED)
			.where(qWorkspaceWorkerSchedule.id.eq(workerScheduleId))
			.execute();
	}
}
