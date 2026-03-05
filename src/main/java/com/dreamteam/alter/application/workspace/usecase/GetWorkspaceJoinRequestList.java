package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceJoinRequestResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceJoinRequestListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessJoinRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getWorkspaceJoinRequestList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetWorkspaceJoinRequestList implements GetWorkspaceJoinRequestListUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final BusinessJoinRequestQueryRepository businessJoinRequestQueryRepository;

    @Override
    public List<WorkspaceJoinRequestResponseDto> execute(ManagerActor actor, Long workspaceId) {
        Workspace workspace = workspaceQueryRepository.findById(workspaceId)
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 업장의 관리자가 아닙니다.");
        }

        return businessJoinRequestQueryRepository.findPendingByWorkspace(workspace).stream()
            .map(WorkspaceJoinRequestResponseDto::from)
            .toList();
    }
}
