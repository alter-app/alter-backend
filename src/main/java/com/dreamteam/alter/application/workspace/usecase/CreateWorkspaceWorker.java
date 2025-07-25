package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceWorkerUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerReadOnlyRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("createWorkspaceWorker")
@RequiredArgsConstructor
@Transactional
public class CreateWorkspaceWorker implements CreateWorkspaceWorkerUseCase {

    private final WorkspaceWorkerRepository workspaceWorkerRepository;
    private final WorkspaceWorkerReadOnlyRepository workspaceWorkerReadOnlyRepository;

    @Override
    public void execute(Workspace workspace, User user) {
        // 이미 근무중인 사용자인지 확인
        if (workspaceWorkerReadOnlyRepository.findActiveWorkerByWorkspaceAndUser(workspace, user).isPresent()) {
            throw new CustomException(ErrorCode.WORKSPACE_WORKER_ALREADY_EXISTS);
        }

        workspaceWorkerRepository.save(WorkspaceWorker.create(workspace, user));
    }

}
