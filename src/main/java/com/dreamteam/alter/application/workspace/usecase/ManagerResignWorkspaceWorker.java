package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerResignWorkspaceWorkerUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.WorkerResignationService;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("managerResignWorkspaceWorker")
@RequiredArgsConstructor
@Transactional
public class ManagerResignWorkspaceWorker implements ManagerResignWorkspaceWorkerUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;
    private final WorkerResignationService workerResignationService;

    @Override
    public void execute(ManagerActor actor, Long workspaceId, Long workerId) {
        if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser())) {
            throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);
        }

        WorkspaceWorker worker = workspaceWorkerQueryRepository.findById(workerId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "근무자를 찾을 수 없습니다."));

        if (!worker.getWorkspace().getId().equals(workspaceId)) {
            throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);
        }

        workerResignationService.resign(worker);
    }
}
