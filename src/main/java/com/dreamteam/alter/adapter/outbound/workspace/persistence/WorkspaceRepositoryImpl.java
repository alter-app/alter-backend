package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceRepositoryImpl implements WorkspaceRepository {

	private final WorkspaceJpaRepository workspaceJpaRepository;

	@Override
	public void save(Workspace workspace) {
		workspaceJpaRepository.save(workspace);
	}
}
