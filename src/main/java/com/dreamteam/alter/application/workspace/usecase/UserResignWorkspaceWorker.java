package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.inbound.UserResignWorkspaceWorkerUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.WorkerResignationService;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userResignWorkspaceWorker")
@RequiredArgsConstructor
@Transactional
public class UserResignWorkspaceWorker implements UserResignWorkspaceWorkerUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;
    private final WorkerResignationService workerResignationService;

    @Override
    public void execute(AppActor actor, Long workspaceId) {
        Workspace workspace = workspaceQueryRepository.findById(workspaceId)
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        WorkspaceWorker worker = workspaceWorkerQueryRepository
            .findActiveWorkerByWorkspaceAndUser(workspace, actor.getUser())
            .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "해당 업장에서 근무중이 아닙니다."));

        workerResignationService.resign(worker);
    }
}
