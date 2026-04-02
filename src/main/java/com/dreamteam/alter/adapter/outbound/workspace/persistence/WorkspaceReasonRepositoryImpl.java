package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceReason;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceReasonRepositoryImpl implements WorkspaceReasonRepository {

	private final WorkspaceReasonJpaRepository workspaceReasonJpaRepository;

	@Override
	public void save(WorkspaceReason reason) {
		workspaceReasonJpaRepository.save(reason);
	}
}
