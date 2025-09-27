package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerScheduleResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetScheduleListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("managerGetWorkScheduleList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerGetWorkScheduleList implements ManagerGetScheduleListUseCase {

    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;
    private final WorkspaceQueryRepository workspaceQueryRepository;

    @Override
    public List<ManagerScheduleResponseDto> execute(ManagerActor actor, Long workspaceId, int year, int month) {
        // 워크스페이스 존재 확인
        Optional<Workspace> workspace = workspaceQueryRepository.findById(workspaceId);
        if (workspace.isEmpty()) {
            throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);
        }

        // 매니저 권한 검증
        if (!workspace.get().getManagerUser().equals(actor.getManagerUser())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "관리 중인 업장이 아닙니다.");
        }

        List<WorkspaceShift> shifts = workspaceShiftQueryRepository
            .findByManagerAndDateRange(actor.getManagerUser(), workspaceId, year, month);
        
        return shifts.stream()
            .map(ManagerScheduleResponseDto::of)
            .toList();
    }
}
