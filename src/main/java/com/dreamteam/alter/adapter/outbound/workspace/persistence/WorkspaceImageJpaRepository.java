package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceImage;

public interface WorkspaceImageJpaRepository extends JpaRepository<WorkspaceImage, Long> {
    List<WorkspaceImage> findByWorkspaceIdOrderBySortOrderAsc(Long workspaceId);
    long countByWorkspaceId(Long workspaceId);
}
