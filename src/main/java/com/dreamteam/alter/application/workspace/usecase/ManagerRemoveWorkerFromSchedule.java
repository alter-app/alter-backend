package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerRemoveWorkerUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service("managerRemoveWorkerFromSchedule")
@RequiredArgsConstructor
@Transactional
public class ManagerRemoveWorkerFromSchedule implements ManagerRemoveWorkerUseCase {

    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;
    private final NotificationService notificationService;

    @Override
    public void execute(ManagerActor actor, Long shiftId) {
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
        
        // 제거될 근무자 정보 저장 (알림 전송용)
        WorkspaceWorker assignedWorker = workspaceShift.getAssignedWorkspaceWorker();
        
        workspaceShift.unassignWorker();
        
        // 근무자에게 스케줄 제거 알림 전송
        if (assignedWorker != null) {
            sendScheduleRemovalNotification(workspaceShift, assignedWorker);
        }
    }
    
    private void sendScheduleRemovalNotification(WorkspaceShift shift, WorkspaceWorker worker) {
        try {
            String workspaceName = shift.getWorkspace().getBusinessName();
            String date = shift.getStartDateTime().format(DateTimeFormatter.ofPattern("MM월 dd일"));
            String time = String.format("%s - %s", 
                shift.getStartDateTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                shift.getEndDateTime().format(DateTimeFormatter.ofPattern("HH:mm"))
            );
            String position = shift.getPosition();
            
            String title = "근무 스케줄이 취소되었습니다";
            String body = String.format("%s - %s %s %s 근무가 취소되었습니다", 
                workspaceName, date, time, position);
            
            notificationService.sendNotification(
                FcmNotificationRequestDto.of(worker.getUser().getId(), title, body)
            );
        } catch (CustomException e) {
            // 알림 발송 실패는 스케줄 제거 프로세스에 영향을 주지 않음
        }
    }
}
