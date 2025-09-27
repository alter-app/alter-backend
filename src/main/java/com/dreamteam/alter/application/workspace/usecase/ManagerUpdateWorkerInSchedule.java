package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerUpdateWorkerUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("managerUpdateWorkerInSchedule")
@RequiredArgsConstructor
@Transactional
public class ManagerUpdateWorkerInSchedule implements ManagerUpdateWorkerUseCase {

    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;
    private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;

    @Override
    public void execute(ManagerActor actor, Long shiftId, Long newWorkerId) {
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

        // 기존 근무자 존재 확인
        if (ObjectUtils.isEmpty(workspaceShift.getAssignedWorkspaceWorker())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "기존에 배정된 근무자가 없는 스케줄입니다.");
        }

        // 새로운 근무자 존재 확인
        Optional<WorkspaceWorker> newWorkspaceWorker = workspaceWorkerQueryRepository.findById(newWorkerId);
        if (newWorkspaceWorker.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "해당 업장의 근무자가 아닙니다.");
        }

        // 워크스페이스 일치 확인
        if (!newWorkspaceWorker.get().getWorkspace().equals(workspaceShift.getWorkspace())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "해당 업장의 근무자가 아닙니다.");
        }

        // 새로운 근무자가 이미 같은 시간대에 배정된 스케줄이 있는지 확인
        if (workspaceShiftQueryRepository.hasConflictingSchedule(
            newWorkspaceWorker.get(), 
            workspaceShift.getStartDateTime(), 
            workspaceShift.getEndDateTime()
        )) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "해당 근무자가 이미 같은 시간대에 배정된 스케줄이 있습니다.");
        }

        // 근무자 배정 (기존 근무자가 있으면 자동으로 교체됨)
        workspaceShift.assignWorker(newWorkspaceWorker.get());
    }
}
