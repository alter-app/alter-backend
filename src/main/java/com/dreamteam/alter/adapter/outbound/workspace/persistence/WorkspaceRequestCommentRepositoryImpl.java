package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequestComment;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestCommentRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceRequestCommentRepositoryImpl implements WorkspaceRequestCommentRepository {

	private final WorkspaceRequestCommentJpaRepository workspaceRequestCommentJpaRepository;

	@Override
	public WorkspaceRequestComment save(WorkspaceRequestComment comment) {
		return workspaceRequestCommentJpaRepository.save(comment);
	}
}
