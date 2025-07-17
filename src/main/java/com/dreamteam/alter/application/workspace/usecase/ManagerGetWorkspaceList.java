package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceListResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetWorkspaceListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("managerGetWorkspaceList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerGetWorkspaceList implements ManagerGetWorkspaceListUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;

    @Override
    public List<ManagerWorkspaceListResponseDto> execute(ManagerActor actor) {
        return workspaceQueryRepository.getManagerWorkspaceList(actor.getManagerUser())
            .stream()
            .map(ManagerWorkspaceListResponseDto::of)
            .toList();
    }

}
