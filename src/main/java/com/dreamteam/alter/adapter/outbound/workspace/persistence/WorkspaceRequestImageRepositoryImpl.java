package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequestImage;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestImageRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceRequestImageRepositoryImpl implements WorkspaceRequestImageRepository {

    private final WorkspaceRequestImageJpaRepository workspaceRequestImageJpaRepository;

    @Override
    public void saveAll(List<WorkspaceRequestImage> workspaceRequestImages) {
        workspaceRequestImageJpaRepository.saveAll(workspaceRequestImages);
    }
}
