package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceImage;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceImageQueryRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceImageQueryRepositoryImpl implements WorkspaceImageQueryRepository {

    private final WorkspaceImageJpaRepository workspaceImageJpaRepository;

    @Override
    public List<WorkspaceImage> findAllByWorkspaceId(Long workspaceId) {
        return workspaceImageJpaRepository.findByWorkspaceIdOrderBySortOrderAsc(workspaceId);
    }
}
