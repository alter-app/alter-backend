package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.UpdateWorkspaceWorkerColorRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerUpdateWorkspaceWorkerColorCodeUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("managerUpdateWorkspaceWorkerColorCode")
@RequiredArgsConstructor
@Transactional
public class ManagerUpdateWorkspaceWorkerColorCode implements ManagerUpdateWorkspaceWorkerColorCodeUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;

    @Override
    public void execute(
        ManagerActor actor,
        Long workspaceId,
        Long workerId,
        UpdateWorkspaceWorkerColorRequestDto request
    ) {
        if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser())) {
            throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);
        }

        WorkspaceWorker worker = workspaceWorkerQueryRepository.findById(workerId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "근무자를 찾을 수 없습니다"));

        if (!worker.getWorkspace().getId().equals(workspaceId)) {
            throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);
        }

        if (!WorkspaceWorker.DEFAULT_COLOR_CODE.equals(request.getColorCode())
            && workspaceWorkerQueryRepository.existsActivatedByWorkspaceAndColorCode(
                workspaceId,
                request.getColorCode(),
                workerId
            )) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 사용 중인 근무자 색상입니다.");
        }

        worker.updateColorCode(request.getColorCode());
    }
}
