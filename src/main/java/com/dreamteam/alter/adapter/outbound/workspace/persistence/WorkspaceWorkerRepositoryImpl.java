package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WorkspaceWorkerRepositoryImpl implements WorkspaceWorkerRepository {

    private final WorkspaceWorkerJpaRepository workspaceWorkerJpaRepository;

    @Override
    public void save(WorkspaceWorker workspaceWorker) {
        workspaceWorkerJpaRepository.save(workspaceWorker);
    }

}
