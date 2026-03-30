package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.QWorkspace;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceRequest;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceRequestQueryRepositoryImpl implements WorkspaceRequestQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public boolean existsByIdAndUserId(Long workspaceRequestId, Long userId) {
		QWorkspaceRequest qWorkspaceRequest = QWorkspaceRequest.workspaceRequest;

		Integer result = queryFactory
			.selectOne()
			.from(qWorkspaceRequest)
			.where(
				qWorkspaceRequest.id.eq(workspaceRequestId),
				qWorkspaceRequest.user.id.eq(userId)
			)
			.fetchFirst();

		return result != null;
	}
}
