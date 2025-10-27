package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.notification.NotificationMessageBuilder;
import com.dreamteam.alter.common.notification.NotificationMessageConstants;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerAssignWorkerUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service("managerAssignWorkerToSchedule")
@RequiredArgsConstructor
@Transactional
public class ManagerAssignWorkerToSchedule implements ManagerAssignWorkerUseCase {

    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;
    private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;
    private final NotificationService notificationService;

    @Override
    public void execute(ManagerActor actor, Long shiftId, Long workerId) {
        // 스케줄 존재 확인
        Optional<WorkspaceShift> shift = workspaceShiftQueryRepository.findById(shiftId);
        if (shift.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 근무 스케줄입니다.");
        }

        // 매니저 권한 검증
        if (!shift.get().getWorkspace().getManagerUser().equals(actor.getManagerUser())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "관리 중인 업장이 아닙니다.");
        }

        // 워크스페이스 근무자 존재 확인
        Optional<WorkspaceWorker> workspaceWorker = workspaceWorkerQueryRepository.findById(workerId);
        if (workspaceWorker.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "해당 업장의 근무자가 아닙니다.");
        }

        // 워크스페이스 일치 확인
        if (!workspaceWorker.get().getWorkspace().equals(shift.get().getWorkspace())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "해당 업장의 근무자가 아닙니다.");
        }

        WorkspaceShift workspaceShift = shift.get();
        
        // 이미 배정된 근무자가 있는지 확인
        if (ObjectUtils.isNotEmpty(workspaceShift.getAssignedWorkspaceWorker())) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 근무자가 배정된 스케줄입니다.");
        }
        
        // 해당 근무자가 이미 같은 시간대에 배정된 스케줄이 있는지 확인
        if (workspaceShiftQueryRepository.hasConflictingSchedule(
            workspaceWorker.get(), 
            workspaceShift.getStartDateTime(), 
            workspaceShift.getEndDateTime()
        )) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "해당 근무자가 이미 같은 시간대에 배정된 스케줄이 있습니다.");
        }

        workspaceShift.assignWorker(workspaceWorker.get());
        
        // 근무자에게 스케줄 배정 알림 전송
        sendScheduleAssignmentNotification(workspaceShift, workspaceWorker.get());
    }
    
    private void sendScheduleAssignmentNotification(WorkspaceShift shift, WorkspaceWorker worker) {
        try {
            String title = NotificationMessageConstants.Schedule.ASSIGNMENT_TITLE;
            String body = NotificationMessageBuilder.buildScheduleAssignmentMessage(
                shift.getWorkspace().getBusinessName(),
                shift.getStartDateTime(),
                shift.getEndDateTime(),
                shift.getPosition()
            );
            
            notificationService.sendNotification(
                FcmNotificationRequestDto.of(worker.getUser().getId(), TokenScope.APP, title, body)
            );
        } catch (CustomException e) {
            // 알림 발송 실패는 스케줄 배정 프로세스에 영향을 주지 않음
        }
    }
}
