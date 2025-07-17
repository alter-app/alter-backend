package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceResponseDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetWorkspaceUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("managerGetWorkspace")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerGetWorkspace implements ManagerGetWorkspaceUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;

    @Override
    public ManagerWorkspaceResponseDto execute(ManagerActor actor, Long workspaceId) {
        Optional<ManagerWorkspaceResponse> workspace =
            workspaceQueryRepository.getByManagerUserAndId(actor.getManagerUser(), workspaceId);

        if (workspace.isEmpty()) {
            throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);
        }

        return ManagerWorkspaceResponseDto.of(workspace.get());
    }

}
