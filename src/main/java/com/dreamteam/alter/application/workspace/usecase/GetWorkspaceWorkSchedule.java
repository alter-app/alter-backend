package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.constants.WorkspaceConstants;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.GetWorkspaceScheduleResponseDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkScheduleInquiryRequestDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkspaceScheduleResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service("getWorkspaceWorkSchedule")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetWorkspaceWorkSchedule implements GetWorkspaceScheduleUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;
    private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;
    @Override
    public GetWorkspaceScheduleResponseDto execute(AppActor actor, Long workspaceId, WorkScheduleInquiryRequestDto request) {
        if (ObjectUtils.isEmpty(request.getYear()) || ObjectUtils.isEmpty(request.getMonth())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "근무일정 조회 시 연도, 월 파라미터는 필수입니다.");
        }

        // 워크스페이스 존재 확인 및 사용자가 해당 워크스페이스에 근무중인지 확인
        Workspace workspace = workspaceQueryRepository.findById(workspaceId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        Optional<WorkspaceWorker> workspaceWorker = workspaceWorkerQueryRepository
            .findActiveWorkerByWorkspaceAndUser(workspace, actor.getUser());

        if (workspaceWorker.isEmpty()) {
            throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);
        }

        List<WorkspaceShift> shifts = workspaceShiftQueryRepository
            .findByWorkspaceAndDateRange(workspaceWorker.get().getWorkspace(), request.getYear(), request.getMonth());

        // 본인에게 배정된 근무 일정들의 근무 시간 합산 및 예상 급여 계산
        double myTotalWorkHours = shifts.stream()
            .filter(shift -> workspaceWorker.get().equals(shift.getAssignedWorkspaceWorker()))
            .mapToDouble(shift -> Duration.between(shift.getStartDateTime(), shift.getEndDateTime()).toMinutes() / 60.0)
            .sum();
        long estimatedSalary = Math.round(myTotalWorkHours * WorkspaceConstants.MINIMUM_HOURLY_WAGE);

        List<WorkspaceScheduleResponseDto> scheduleDtos = shifts.stream()
            .map(WorkspaceScheduleResponseDto::of)
            .toList();

        return GetWorkspaceScheduleResponseDto.of(myTotalWorkHours, estimatedSalary, scheduleDtos);
    }
}
