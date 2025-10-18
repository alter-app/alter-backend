package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.notification.NotificationMessageConstants;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.inbound.AcceptSubstituteRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("acceptSubstituteRequest")
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AcceptSubstituteRequest implements AcceptSubstituteRequestUseCase {

    private final SubstituteRequestQueryRepository substituteRequestQueryRepository;
    private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;
    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;
    private final NotificationService notificationService;

    @Override
    public void execute(AppActor actor, Long requestId) {
        // 대타 요청 조회
        SubstituteRequest request = substituteRequestQueryRepository.findById(requestId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 대타 요청입니다."));

        // 현재 사용자의 WorkspaceWorker 정보 조회
        WorkspaceWorker myWorker = workspaceWorkerQueryRepository
            .findActiveWorkerByWorkspaceAndUser(request.getWorkspaceShift().getWorkspace(), actor.getUser())
            .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "해당 업장의 근무자가 아닙니다."));

        // 수락 가능 여부 검증
        if (!request.canBeAcceptedBy(myWorker.getId())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "수락할 수 없는 요청입니다.");
        }

        // 시간대 겹침 검증
        if (workspaceShiftQueryRepository.hasConflictingSchedule(
            myWorker,
            request.getWorkspaceShift().getStartDateTime(),
            request.getWorkspaceShift().getEndDateTime()
        )) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 해당 시간대에 근무 스케줄이 있습니다.");
        }

        // 수락 처리
        request.accept(myWorker.getId());

        // 알림 발송 (요청자와 점주/매니저에게)
        sendAcceptanceNotification(request, myWorker.getUser().getName());
    }

    private void sendAcceptanceNotification(SubstituteRequest request, String accepterName) {
        try {
            // 요청자 조회
            WorkspaceWorker requesterWorker = workspaceWorkerQueryRepository.findById(request.getRequesterId())
                .orElse(null);

            if (ObjectUtils.isNotEmpty(requesterWorker)) {
                String title = NotificationMessageConstants.SubstituteRequest.ACCEPTED_TITLE;
                String body = String.format(
                    NotificationMessageConstants.SubstituteRequest.ACCEPTED_BODY,
                    accepterName,
                    request.getWorkspaceShift().getStartDateTime().toLocalDate(),
                    request.getWorkspaceShift().getStartDateTime().toLocalTime()
                );

                notificationService.sendNotification(
                    FcmNotificationRequestDto.of(requesterWorker.getUser().getId(), title, body)
                );
            }

            // 점주/매니저에게 알림 (승인 대기 상태로 변경됨을 알림)
            ManagerUser managerUser = request.getWorkspaceShift()
                .getWorkspace()
                .getManagerUser();

            String managerTitle = "대타 요청이 수락되었습니다";
            String managerBody = String.format(
                "%s님이 %s %s 근무 대타를 수락했습니다. 승인을 기다리고 있습니다.",
                accepterName,
                request.getWorkspaceShift().getStartDateTime().toLocalDate(),
                request.getWorkspaceShift().getStartDateTime().toLocalTime()
            );

            notificationService.sendNotification(
                FcmNotificationRequestDto.of(managerUser.getUser().getId(), managerTitle, managerBody)
            );
        } catch (CustomException e) {
            // 알림 발송 실패는 수락 프로세스에 영향을 주지 않음
        }
    }
}
