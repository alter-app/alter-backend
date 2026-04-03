package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceReasonCommentListResponse;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceReasonComment;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonCommentQueryRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceReasonCommentQueryRepositoryImpl implements WorkspaceReasonCommentQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<WorkspaceReasonCommentListResponse> getWorkspaceReasonCommentList(Long reasonId) {
		QWorkspaceReasonComment q = QWorkspaceReasonComment.workspaceReasonComment;

		return queryFactory
			.select(Projections.constructor(
				WorkspaceReasonCommentListResponse.class,
				q.id,
				q.workspaceReason.id,
				q.user.id,
				q.commentOwner,
				q.comment,
				q.createdAt
			))
			.from(q)
			.where(q.workspaceReason.id.eq(reasonId))
			.orderBy(q.createdAt.asc(), q.id.asc())
			.fetch();
	}
}
