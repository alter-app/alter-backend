package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.model.WorkspaceRequestCommentListResponse;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceRequestComment;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestCommentQueryRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceRequestCommentQueryRepositoryImpl implements WorkspaceRequestCommentQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<WorkspaceRequestCommentListResponse> getCommentList(Long workspaceRequestId) {
		QWorkspaceRequestComment q = QWorkspaceRequestComment.workspaceRequestComment;

		return queryFactory
			.select(Projections.constructor(
				WorkspaceRequestCommentListResponse.class,
				q.id,
				q.workspaceRequest.id,
				q.user.id,
				q.commentOwner,
				q.comment,
				q.createdAt
			))
			.from(q)
			.where(q.workspaceRequest.id.eq(workspaceRequestId))
			.orderBy(q.createdAt.asc(), q.id.asc())
			.fetch();
	}
}
