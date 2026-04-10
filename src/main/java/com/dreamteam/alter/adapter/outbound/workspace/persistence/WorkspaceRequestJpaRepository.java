package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;

public interface WorkspaceRequestJpaRepository extends JpaRepository<WorkspaceRequest, Long> {
}
