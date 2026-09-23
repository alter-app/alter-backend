package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.UpdateWorkScheduleRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerUpdateScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("managerUpdateWorkSchedule")
@RequiredArgsConstructor
@Transactional
public class ManagerUpdateWorkSchedule implements ManagerUpdateScheduleUseCase {

    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;

    @Override
    public void execute(ManagerActor actor, Long shiftId, UpdateWorkScheduleRequestDto request) {
        // 스케줄 존재 확인
        Optional<WorkspaceShift> shift = workspaceShiftQueryRepository.findById(shiftId);
        if (shift.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 근무 스케줄입니다.");
        }

        // 매니저 권한 검증
        if (!shift.get().getWorkspace().getManagerUser().equals(actor.getManagerUser())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "관리 중인 업장이 아닙니다.");
        }

        WorkspaceShift workspaceShift = shift.get();

        // 배정된 근무자가 있으면 바뀐 시간대가 그 근무자의 다른 근무와 겹치는지 재검증한다 (자기 자신은 제외)
        if (ObjectUtils.isNotEmpty(workspaceShift.getAssignedWorkspaceWorker())
            && workspaceShiftQueryRepository.hasConflictingSchedule(
                workspaceShift.getAssignedWorkspaceWorker(),
                request.getStartDateTime(),
                request.getEndDateTime(),
                shiftId
        )) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "해당 근무자가 이미 같은 시간대에 배정된 스케줄이 있습니다.");
        }

        workspaceShift.update(
            request.getStartDateTime(),
            request.getEndDateTime(),
            request.getPosition()
        );   
    }
}
