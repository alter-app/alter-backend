package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestResponse;
import com.dreamteam.alter.domain.file.entity.QFile;
import com.dreamteam.alter.domain.file.type.FileStatus;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceRequest;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
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

	@Override
	public WorkspaceRequestResponse getWorkspaceRequest(Long userId, Long workspaceRequestId) {
		QWorkspaceRequest qWorkspaceRequest = QWorkspaceRequest.workspaceRequest;
		QFile certFile = new QFile("certFile");
		QFile ownIdentityFile = new QFile("ownIdentityFile");
		QFile warrantFile = new QFile("warrantFile");
		String targetId = workspaceRequestId.toString();

		return queryFactory
			.select(Projections.constructor(
				WorkspaceRequestResponse.class,
				qWorkspaceRequest.id,
				qWorkspaceRequest.businessRegistrationNo,
				qWorkspaceRequest.businessName,
				qWorkspaceRequest.businessType,
				qWorkspaceRequest.contact,
				qWorkspaceRequest.fullAddress,
				qWorkspaceRequest.latitude,
				qWorkspaceRequest.longitude,
				qWorkspaceRequest.status,
				JPAExpressions
					.select(certFile.id)
					.from(certFile)
					.where(
						certFile.targetId.eq(targetId),
						certFile.targetType.eq(FileTargetType.WORKSPACE_CERTIFICATE),
						certFile.status.eq(FileStatus.ATTACHED)
					),
				JPAExpressions
					.select(ownIdentityFile.id)
					.from(ownIdentityFile)
					.where(
						ownIdentityFile.targetId.eq(targetId),
						ownIdentityFile.targetType.eq(FileTargetType.WORKSPACE_OWN_IDENTITY),
						ownIdentityFile.status.eq(FileStatus.ATTACHED)
					),
				JPAExpressions
					.select(warrantFile.id)
					.from(warrantFile)
					.where(
						warrantFile.targetId.eq(targetId),
						warrantFile.targetType.eq(FileTargetType.WORKSPACE_WARRANT),
						warrantFile.status.eq(FileStatus.ATTACHED)
					),
				qWorkspaceRequest.createdAt,
				qWorkspaceRequest.updatedAt
			))
			.from(qWorkspaceRequest)
			.where(qWorkspaceRequest.user.id.eq(userId)
				.and(qWorkspaceRequest.id.eq(workspaceRequestId)))
			.fetchOne();
	}

	@Override
	public Optional<WorkspaceRequest> findById(Long workspaceRequestId) {
		QWorkspaceRequest qWorkspaceRequest = QWorkspaceRequest.workspaceRequest;

		return Optional.ofNullable(
			queryFactory.selectFrom(qWorkspaceRequest)
				.where(qWorkspaceRequest.id.eq(workspaceRequestId))
				.fetchOne()
		);
	}
}
