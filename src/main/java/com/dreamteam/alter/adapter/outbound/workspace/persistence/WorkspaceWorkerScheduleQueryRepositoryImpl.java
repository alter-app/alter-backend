package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.QWorkspace;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerScheduleStatus;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceWorkerScheduleQueryRepositoryImpl implements WorkspaceWorkerScheduleQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Optional<WorkspaceWorkerSchedule> findById(Long workerScheduleId) {
		QWorkspaceWorkerSchedule qWorkspaceWorkerSchedule = QWorkspaceWorkerSchedule.workspaceWorkerSchedule;

		return Optional.ofNullable(
			queryFactory
				.selectFrom(qWorkspaceWorkerSchedule)
				.where(
					qWorkspaceWorkerSchedule.id.eq(workerScheduleId),
					qWorkspaceWorkerSchedule.status.ne(WorkspaceWorkerScheduleStatus.DELETED)
				)
				.fetchOne()
		);
	}

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
	public List<WorkspaceWorkerSchedule> getByWorkspaceWorker(WorkspaceWorker workspaceWorker) {
		QWorkspaceWorkerSchedule qWorkspaceWorkerSchedule = QWorkspaceWorkerSchedule.workspaceWorkerSchedule;

		return queryFactory
			.selectFrom(qWorkspaceWorkerSchedule)
			.where(
				qWorkspaceWorkerSchedule.workspaceWorker.eq(workspaceWorker),
				qWorkspaceWorkerSchedule.status.ne(WorkspaceWorkerScheduleStatus.DELETED)
			)
			.fetch();
	}

	@Override
	public List<WorkspaceWorkerSchedule> findAllActivatedWithWorkspaceWorkerByWorkspaceIds(List<Long> workspaceIds) {
		QWorkspaceWorkerSchedule qWorkspaceWorkerSchedule = QWorkspaceWorkerSchedule.workspaceWorkerSchedule;
		QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;
		QWorkspace qWorkspace = QWorkspace.workspace;

		return queryFactory
			.selectFrom(qWorkspaceWorkerSchedule)
			.join(qWorkspaceWorkerSchedule.workspaceWorker, qWorkspaceWorker).fetchJoin()
			.join(qWorkspaceWorker.workspace, qWorkspace).fetchJoin()
			.where(
				qWorkspace.id.in(workspaceIds),
				qWorkspaceWorkerSchedule.status.eq(WorkspaceWorkerScheduleStatus.ACTIVATED),
				qWorkspaceWorker.status.eq(WorkspaceWorkerStatus.ACTIVATED)
			)
			.fetch();
	}

	@Override
	public List<WorkspaceWorkerSchedule> findAllActivatedByUserId(Long userId) {
		QWorkspaceWorkerSchedule qWorkspaceWorkerSchedule = QWorkspaceWorkerSchedule.workspaceWorkerSchedule;
		QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;

		return queryFactory
			.selectFrom(qWorkspaceWorkerSchedule)
			.join(qWorkspaceWorkerSchedule.workspaceWorker, qWorkspaceWorker)
			.where(
				qWorkspaceWorker.user.id.eq(userId),
				qWorkspaceWorkerSchedule.status.eq(WorkspaceWorkerScheduleStatus.ACTIVATED)
			)
			.fetch();
	}
}
