package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerTodayScheduleResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetDailyScheduleListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("managerGetTodayWorkScheduleList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerGetDailyWorkScheduleList implements ManagerGetDailyScheduleListUseCase {

    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;
    private final WorkspaceQueryRepository workspaceQueryRepository;

    @Override
    public List<ManagerTodayScheduleResponseDto> execute(ManagerActor actor, Long workspaceId) {
        if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser())) {
            throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);
        }

        return ManagerTodayScheduleResponseDto.from(workspaceShiftQueryRepository.getTodayShiftList(workspaceId));
    }
}
