package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.CreateWorkScheduleRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerCreateScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("managerCreateWorkSchedule")
@RequiredArgsConstructor
@Transactional
public class ManagerCreateWorkSchedule implements ManagerCreateScheduleUseCase {

    private final WorkspaceShiftRepository workspaceShiftRepository;
    private final WorkspaceQueryRepository workspaceQueryRepository;

    @Override
    public void execute(ManagerActor actor, CreateWorkScheduleRequestDto request) {
        // 워크스페이스 존재 확인
        Optional<Workspace> workspace = workspaceQueryRepository
            .findById(request.getWorkspaceId());
        
        if (workspace.isEmpty()) {
            throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);
        }

        // 매니저 권한 검증
        if (!workspace.get().getManagerUser().equals(actor.getManagerUser())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "관리 중인 업장이 아닙니다.");
        }
        WorkspaceShift shift = WorkspaceShift.create(
            workspace.get(),
            request.getStartDateTime(),
            request.getEndDateTime(),
            request.getPosition(),
            WorkspaceShiftStatus.PLANNED
        );
        
        workspaceShiftRepository.save(shift);
    }
}
