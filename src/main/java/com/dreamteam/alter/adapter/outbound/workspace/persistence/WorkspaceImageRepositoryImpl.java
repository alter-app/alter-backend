package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceImage;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceImageRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceImageRepositoryImpl implements WorkspaceImageRepository {

    private final WorkspaceImageJpaRepository workspaceImageJpaRepository;

    @Override
    public void saveAll(List<WorkspaceImage> workspaceImages) {
        workspaceImageJpaRepository.saveAll(workspaceImages);
    }

    @Override
    public void deleteAll(List<WorkspaceImage> workspaceImages) {
        workspaceImageJpaRepository.deleteAll(workspaceImages);
    }
}
