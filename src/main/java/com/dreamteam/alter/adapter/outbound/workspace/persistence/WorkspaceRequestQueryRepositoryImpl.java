package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestListResponse;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceRequest;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;
import com.querydsl.core.types.Projections;
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

	@Override
	public List<WorkspaceRequestListResponse> getWorkspaceRequestList(Long userId) {
		QWorkspaceRequest qWorkspaceRequest = QWorkspaceRequest.workspaceRequest;

		return queryFactory
			.select(Projections.constructor(
				WorkspaceRequestListResponse.class,
				qWorkspaceRequest.id,
				qWorkspaceRequest.businessName,
				qWorkspaceRequest.fullAddress,
				qWorkspaceRequest.createdAt,
				qWorkspaceRequest.status
			))
			.from(qWorkspaceRequest)
			.where(qWorkspaceRequest.user.id.eq(userId))
			.orderBy(qWorkspaceRequest.createdAt.desc())
			.fetch();
	}
}
