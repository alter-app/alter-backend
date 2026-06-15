package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequestImage;

public interface WorkspaceRequestImageJpaRepository extends JpaRepository<WorkspaceRequestImage, Long> {
    List<WorkspaceRequestImage> findByWorkspaceRequestIdOrderBySortOrderAsc(Long workspaceRequestId);
}
