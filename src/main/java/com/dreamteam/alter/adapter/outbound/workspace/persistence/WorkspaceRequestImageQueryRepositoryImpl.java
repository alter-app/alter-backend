package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequestImage;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestImageQueryRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceRequestImageQueryRepositoryImpl implements WorkspaceRequestImageQueryRepository {

    private final WorkspaceRequestImageJpaRepository workspaceRequestImageJpaRepository;

    @Override
    public List<WorkspaceRequestImage> findAllByWorkspaceRequestId(Long workspaceRequestId) {
        return workspaceRequestImageJpaRepository.findByWorkspaceRequestIdOrderBySortOrderAsc(workspaceRequestId);
    }
}
