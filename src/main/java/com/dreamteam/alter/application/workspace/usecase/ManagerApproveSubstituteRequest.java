package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ApproveSubstituteRequestDto;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.notification.NotificationMessageConstants;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerApproveSubstituteRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("managerApproveSubstituteRequest")
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ManagerApproveSubstituteRequest implements ManagerApproveSubstituteRequestUseCase {

    private final SubstituteRequestQueryRepository substituteRequestQueryRepository;
    private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;
    private final NotificationService notificationService;

    @Override
    public void execute(
        ManagerActor actor,
        Long requestId,
        ApproveSubstituteRequestDto request
    ) {
        // 대타 요청 조회
        SubstituteRequest substituteRequest = substituteRequestQueryRepository.findById(requestId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 대타 요청입니다."));

        // 권한 확인
        if (!substituteRequest.getWorkspaceShift().getWorkspace().getManagerUser().equals(actor.getManagerUser())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "관리 중인 업장이 아닙니다.");
        }

        // 상태 확인 (ACCEPTED 상태만 승인 가능)
        if (substituteRequest.getStatus() != SubstituteRequestStatus.ACCEPTED) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "승인할 수 없는 상태의 요청입니다.");
        }

        // 수락한 근무자 조회
        WorkspaceWorker acceptedWorker = workspaceWorkerQueryRepository.findById(substituteRequest.getAcceptedWorkerId())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "수락한 근무자 정보를 찾을 수 없습니다."));

        // 승인 처리
        substituteRequest.approve(actor.getManagerUser().getId(), request.getApprovalComment());
        
        // 실제 스케줄 교환 처리
        substituteRequest.getWorkspaceShift().assignWorker(acceptedWorker);

        // 알림 발송 (요청자와 수락자에게)
        sendManagerApprovalNotification(substituteRequest);
    }

    private void sendManagerApprovalNotification(SubstituteRequest request) {
        try {
            String title = NotificationMessageConstants.SubstituteRequest.MANAGER_APPROVED_TITLE;
            String body = String.format(
                NotificationMessageConstants.SubstituteRequest.MANAGER_APPROVED_BODY,
                request.getWorkspaceShift().getWorkspace().getBusinessName(),
                request.getWorkspaceShift().getStartDateTime().toLocalDate(),
                request.getWorkspaceShift().getStartDateTime().toLocalTime()
            );

            // 요청자에게 알림
            workspaceWorkerQueryRepository.findById(request.getRequesterId())
                .ifPresent(requesterWorker -> {
                    notificationService.sendNotification(
                        FcmNotificationRequestDto.of(requesterWorker.getUser().getId(), TokenScope.APP, title, body)
                    );
                });

            // 수락자에게 알림
            workspaceWorkerQueryRepository.findById(request.getAcceptedWorkerId())
                .ifPresent(acceptedWorker -> {
                    notificationService.sendNotification(
                        FcmNotificationRequestDto.of(acceptedWorker.getUser().getId(), TokenScope.APP, title, body)
                    );
                });
        } catch (CustomException e) {
            // 알림 발송 실패는 승인 프로세스에 영향을 주지 않음
        }
    }
}
