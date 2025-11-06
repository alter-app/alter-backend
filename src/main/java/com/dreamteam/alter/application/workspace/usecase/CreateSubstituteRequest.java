package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmBatchNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.CreateSubstituteRequestDto;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.notification.NotificationMessageConstants;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequestTarget;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateSubstituteRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service("createSubstituteRequest")
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CreateSubstituteRequest implements CreateSubstituteRequestUseCase {

    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;
    private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;
    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final SubstituteRequestRepository substituteRequestRepository;
    private final SubstituteRequestQueryRepository substituteRequestQueryRepository;
    private final NotificationService notificationService;

    @Override
    public void execute(AppActor actor, Long scheduleId, CreateSubstituteRequestDto request) {
        // 스케줄 존재 확인
        WorkspaceShift shift = workspaceShiftQueryRepository.findById(scheduleId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 근무 스케줄입니다."));

        // 과거 스케줄인지 확인
        if (shift.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "과거 스케줄에 대한 대타 요청은 불가능합니다.");
        }

        // 요청자가 해당 스케줄의 배정된 근무자인지 확인
        if (ObjectUtils.isEmpty(shift.getAssignedWorkspaceWorker()) ||
            !shift.getAssignedWorkspaceWorker().getUser().equals(actor.getUser())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "해당 스케줄의 근무자가 아닙니다.");
        }

        // 이미 진행 중인 교환 요청이 있는지 확인
        if (substituteRequestQueryRepository.existsActiveRequestByScheduleAndRequester(
            scheduleId,
            shift.getAssignedWorkspaceWorker().getId()
        )
        ) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 진행 중인 교환 요청이 있습니다.");
        }

        Long targetWorkerId = null;

        // SPECIFIC 타입인 경우 대상자 검증
        if (SubstituteRequestType.SPECIFIC.equals(request.getRequestType())) {
            if (ObjectUtils.isEmpty(request.getTargetId())) {
                throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "특정 대상 요청 시 대상자 ID는 필수입니다.");
            }

            WorkspaceWorker targetWorker = workspaceWorkerQueryRepository.findById(request.getTargetId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 근무자입니다."));

            // 본인을 대상으로 선택하는 것을 방지
            if (targetWorker.getId().equals(shift.getAssignedWorkspaceWorker().getId())) {
                throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "본인을 대상으로 선택할 수 없습니다.");
            }

            // 대상자가 같은 업장 근무자인지 확인
            if (!targetWorker.getWorkspace().equals(shift.getWorkspace())) {
                throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "해당 업장의 근무자가 아닙니다.");
            }

            // 대상자가 해당 시간에 근무 스케줄이 있는지 확인
            if (workspaceShiftQueryRepository.hasConflictingSchedule(targetWorker, shift.getStartDateTime(), shift.getEndDateTime())) {
                throw new CustomException(ErrorCode.CONFLICT, "대상자가 이미 해당 시간대에 근무 스케줄이 있습니다.");
            }

            targetWorkerId = targetWorker.getId();
        }

        // 대타 요청 생성
        if (SubstituteRequestType.ALL.equals(request.getRequestType())) {
            // 전체 공개 요청인 경우 교환 가능한 각 근무자에게 개별 요청 생성
            createRequestsForExchangeableWorkers(shift, shift.getAssignedWorkspaceWorker().getId(), request.getRequestReason(), actor.getUser().getName());
        } else {
            // 특정 대상 요청인 경우 단일 요청 생성
            SubstituteRequest createdRequest = SubstituteRequest.create(
                shift,
                shift.getAssignedWorkspaceWorker().getId(),
                request.getRequestType(),
                request.getRequestReason()
            );
            
            // SPECIFIC 타입인 경우 대상자에 대한 SubstituteRequestTarget 생성
            SubstituteRequestTarget target = SubstituteRequestTarget.create(createdRequest, targetWorkerId);
            createdRequest.getTargets().add(target);
            substituteRequestRepository.save(createdRequest);

            // 알림 발송
            sendSubstituteRequestNotification(createdRequest, shift, actor.getUser().getName());
        }
    }

    private void sendSubstituteRequestNotification(SubstituteRequest request, WorkspaceShift shift, String requesterName) {
        try {
            String title = NotificationMessageConstants.SubstituteRequest.NEW_REQUEST_TITLE;
            String body = String.format(
                NotificationMessageConstants.SubstituteRequest.NEW_REQUEST_BODY,
                requesterName,
                shift.getStartDateTime().toLocalDate(),
                shift.getStartDateTime().toLocalTime()
            );

            // SPECIFIC 타입인 경우 대상자에게 알림 발송
            if (ObjectUtils.isNotEmpty(request.getTargets())) {
                sendNotificationToTargets(request.getTargets(), title, body);
            }
        } catch (CustomException e) {
            // 알림 발송 실패는 대타 요청 생성 프로세스에 영향을 주지 않음
        }
    }


    private void createRequestsForExchangeableWorkers(WorkspaceShift shift, Long requesterId, String requestReason, String requesterName) {
        try {
            // 교환 가능한 근무자 ID 목록 조회
            List<Long> exchangeableWorkerIds = workspaceQueryRepository.getExchangeableWorkerIds(
                shift.getWorkspace().getId(),
                shift.getAssignedWorkspaceWorker().getUser(), // 요청자 본인 제외
                shift.getStartDateTime(),
                shift.getEndDateTime()
            );

            if (ObjectUtils.isEmpty(exchangeableWorkerIds)) {
                log.info("교환 가능한 근무자가 없습니다. workspaceId={}", shift.getWorkspace().getId());
                return;
            }

            // 하나의 SubstituteRequest 생성 (ALL 타입)
            SubstituteRequest request = SubstituteRequest.create(
                shift,
                requesterId,
                SubstituteRequestType.ALL,
                requestReason
            );
            
            // 각 교환 가능한 근무자에 대해 SubstituteRequestTarget 생성
            List<SubstituteRequestTarget> targets = exchangeableWorkerIds.stream()
                .map(workerId -> SubstituteRequestTarget.create(request, workerId))
                .toList();
            
            // SubstituteRequest에 targets 설정
            request.getTargets().addAll(targets);
            
            // 저장
            substituteRequestRepository.save(request);

            // 각 대상자에게 알림 발송
            String title = NotificationMessageConstants.SubstituteRequest.NEW_REQUEST_TITLE;
            String body = String.format(
                NotificationMessageConstants.SubstituteRequest.NEW_REQUEST_BODY,
                requesterName,
                shift.getStartDateTime().toLocalDate(),
                shift.getStartDateTime().toLocalTime()
            );
            sendNotificationToTargets(targets, title, body);

            log.info("전체 공개 대타 요청 생성 완료. 대상자 수={}, workspaceId={}",
                exchangeableWorkerIds.size(), shift.getWorkspace().getId());

        } catch (Exception e) {
            log.error("전체 공개 대타 요청 생성 실패. workspaceId={}, error={}",
                shift.getWorkspace().getId(), e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "대타 요청 생성 중 오류가 발생했습니다.");
        }
    }

    private void sendNotificationToTargets(List<SubstituteRequestTarget> targets, String title, String body) {
        try {
            if (ObjectUtils.isEmpty(targets)) {
                return;
            }

            // 대상자 ID 목록 추출
            List<Long> targetWorkerIds = targets.stream()
                .map(SubstituteRequestTarget::getTargetWorkerId)
                .toList();

            // 배치로 WorkspaceWorker 조회
            List<WorkspaceWorker> targetWorkers = workspaceWorkerQueryRepository.findAllById(targetWorkerIds);

            if (ObjectUtils.isEmpty(targetWorkers)) {
                log.warn("알림 발송 대상 근무자를 찾을 수 없습니다. targetWorkerIds={}", targetWorkerIds);
                return;
            }

            // 사용자 ID 목록 추출
            List<Long> targetUserIds = targetWorkers.stream()
                .map(worker -> worker.getUser().getId())
                .toList();

            // 다중 사용자 알림 발송
            notificationService.sendMultipleNotifications(
                FcmBatchNotificationRequestDto.of(targetUserIds, TokenScope.APP, title, body)
            );

        } catch (CustomException e) {
            // 알림 발송 실패는 대타 요청 생성 프로세스에 영향을 주지 않음
            log.warn("대타 요청 알림 발송 실패: {}", e.getMessage());
        }
    }
}
