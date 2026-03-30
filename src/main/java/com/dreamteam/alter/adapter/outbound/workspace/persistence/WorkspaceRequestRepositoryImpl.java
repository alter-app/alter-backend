package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceRequestRepositoryImpl implements WorkspaceRequestRepository {

	private final WorkspaceRequestJpaRepository workspaceRequestJpaRepository;

	@Override
	public Long save(WorkspaceRequest workspaceRequest) {
		return workspaceRequestJpaRepository.save(workspaceRequest).getId();
	}
}
