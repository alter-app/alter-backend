package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.RejectSubstituteRequestDto;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.notification.NotificationMessageConstants;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.inbound.RejectSubstituteRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("rejectSubstituteRequest")
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RejectSubstituteRequest implements RejectSubstituteRequestUseCase {

    private final SubstituteRequestQueryRepository substituteRequestQueryRepository;
    private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;
    private final NotificationService notificationService;

    @Override
    public void execute(AppActor actor, Long requestId, RejectSubstituteRequestDto request) {
        // 대타 요청 조회
        SubstituteRequest substituteRequest = substituteRequestQueryRepository.findById(requestId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 대타 요청입니다."));

        // 현재 사용자의 WorkspaceWorker 정보 조회
        WorkspaceWorker myWorker = workspaceWorkerQueryRepository
            .findActiveWorkerByWorkspaceAndUser(substituteRequest.getWorkspaceShift().getWorkspace(), actor.getUser())
            .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "해당 업장의 근무자가 아닙니다."));

        // 거절 가능 여부 검증 (특정 대상 요청이고, 본인이 대상자인 경우만)
        if (!substituteRequest.canBeRejectedBy(myWorker.getId())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "거절할 수 없는 요청입니다.");
        }

        // 거절 처리
        substituteRequest.rejectByTarget(myWorker.getId(), request.getTargetRejectionReason());

        // 알림 발송 (요청자에게)
        sendRejectionNotification(substituteRequest, myWorker.getUser().getName());
    }

    private void sendRejectionNotification(SubstituteRequest request, String rejectorName) {
        try {
            // 요청자 조회
            WorkspaceWorker requesterWorker = workspaceWorkerQueryRepository.findById(request.getRequesterId())
                .orElse(null);

            if (ObjectUtils.isEmpty(requesterWorker)) {
                return;
            }

            String title = NotificationMessageConstants.SubstituteRequest.REJECTED_TITLE;
            String body = String.format(
                NotificationMessageConstants.SubstituteRequest.REJECTED_BODY,
                rejectorName,
                request.getWorkspaceShift().getStartDateTime().toLocalDate(),
                request.getWorkspaceShift().getStartDateTime().toLocalTime()
            );

            notificationService.sendNotification(
                FcmNotificationRequestDto.of(requesterWorker.getUser().getId(), title, body)
            );
        } catch (CustomException e) {
            // 알림 발송 실패는 거절 프로세스에 영향을 주지 않음
        }
    }
}

