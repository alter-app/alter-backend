package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestResponse;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceRequest;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
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
	public boolean existsById(Long workspaceRequestId) {
		QWorkspaceRequest qWorkspaceRequest = QWorkspaceRequest.workspaceRequest;

		Integer result = queryFactory
			.selectOne()
			.from(qWorkspaceRequest)
			.where(qWorkspaceRequest.id.eq(workspaceRequestId))
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

	@Override
	public WorkspaceRequestResponse getWorkspaceRequest(Long userId, Long workspaceRequestId) {
		QWorkspaceRequest qWorkspaceRequest = QWorkspaceRequest.workspaceRequest;

		return queryFactory
			.select(Projections.constructor(
				WorkspaceRequestResponse.class,
				qWorkspaceRequest.id,
				qWorkspaceRequest.businessRegistrationNo,
				qWorkspaceRequest.businessName,
				qWorkspaceRequest.businessType.name,
				qWorkspaceRequest.businessTypeDetail,
				qWorkspaceRequest.contact,
				qWorkspaceRequest.fullAddress,
				qWorkspaceRequest.latitude,
				qWorkspaceRequest.longitude,
				qWorkspaceRequest.status,
				qWorkspaceRequest.createdAt,
				qWorkspaceRequest.updatedAt
			))
			.from(qWorkspaceRequest)
			.where(qWorkspaceRequest.user.id.eq(userId)
				.and(qWorkspaceRequest.id.eq(workspaceRequestId)))
			.fetchOne();
	}

	@Override
	public Optional<WorkspaceRequest> findByIdWithUser(Long workspaceRequestId) {
		QWorkspaceRequest qWorkspaceRequest = QWorkspaceRequest.workspaceRequest;

		return Optional.ofNullable(
			queryFactory.selectFrom(qWorkspaceRequest)
				.join(qWorkspaceRequest.user).fetchJoin()
				.where(qWorkspaceRequest.id.eq(workspaceRequestId))
				.fetchOne()
		);
	}

	@Override
	public long countAll() {
		QWorkspaceRequest qWorkspaceRequest = QWorkspaceRequest.workspaceRequest;

		Long count = queryFactory
			.select(qWorkspaceRequest.count())
			.from(qWorkspaceRequest)
			.fetchOne();

		return count != null ? count : 0L;
	}

	@Override
	public List<WorkspaceRequestListResponse> getWorkspaceRequestListWithOffset(PageRequestDto request) {
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
			.orderBy(qWorkspaceRequest.createdAt.desc(), qWorkspaceRequest.id.desc())
			.offset(request.getOffset())
			.limit(request.getLimit())
			.fetch();
	}

	@Override
	public WorkspaceRequestResponse getWorkspaceRequest(Long workspaceRequestId) {
		QWorkspaceRequest qWorkspaceRequest = QWorkspaceRequest.workspaceRequest;

		return queryFactory
			.select(Projections.constructor(
				WorkspaceRequestResponse.class,
				qWorkspaceRequest.id,
				qWorkspaceRequest.businessRegistrationNo,
				qWorkspaceRequest.businessName,
				qWorkspaceRequest.businessType.name,
				qWorkspaceRequest.businessTypeDetail,
				qWorkspaceRequest.contact,
				qWorkspaceRequest.fullAddress,
				qWorkspaceRequest.latitude,
				qWorkspaceRequest.longitude,
				qWorkspaceRequest.status,
				qWorkspaceRequest.createdAt,
				qWorkspaceRequest.updatedAt
			))
			.from(qWorkspaceRequest)
			.where(qWorkspaceRequest.id.eq(workspaceRequestId))
			.fetchOne();
	}

}
