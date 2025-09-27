package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.UpdateWorkScheduleRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerUpdateScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import lombok.RequiredArgsConstructor;
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
        workspaceShift.update(
            request.getStartDateTime(),
            request.getEndDateTime(),
            request.getPosition()
        );   
    }
}
