package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MyScheduleResponseDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkScheduleInquiryRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.port.inbound.GetExchangeableSelfSchedulesUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service("getExchangeableSelfSchedules")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetExchangeableSelfSchedules implements GetExchangeableSelfSchedulesUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;

    @Override
    public List<MyScheduleResponseDto> execute(AppActor actor, Long workspaceId, WorkScheduleInquiryRequestDto request) {
        validateYearMonth(request);

        Workspace workspace = findWorkspace(workspaceId);
        validateActiveWorker(actor, workspaceId);

        LocalDateTime now = LocalDateTime.now();
        ensureNotPastMonth(request, now);

        LocalDateTime fromInclusive = resolveFromInclusive(request, now);

        List<WorkspaceShift> shifts = fetchShifts(actor, workspace, request, fromInclusive);

        return shifts.stream().map(MyScheduleResponseDto::of).toList();
    }

    private void validateYearMonth(WorkScheduleInquiryRequestDto request) {
        if (ObjectUtils.isEmpty(request.getYear()) || ObjectUtils.isEmpty(request.getMonth())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "근무일정 조회 시 연도, 월 파라미터는 필수입니다.");
        }
    }

    private Workspace findWorkspace(Long workspaceId) {
        return workspaceQueryRepository.findById(workspaceId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    private void validateActiveWorker(AppActor actor, Long workspaceId) {
        if (!workspaceQueryRepository.isUserActiveWorkerInWorkspace(actor.getUser(), workspaceId)) {
            throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND, "해당 업장의 근무자가 아닙니다.");
        }
    }

    private void ensureNotPastMonth(WorkScheduleInquiryRequestDto request, LocalDateTime now) {
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        if (request.getYear() < currentYear || (request.getYear() == currentYear && request.getMonth() < currentMonth)) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "과거 스케줄은 조회할 수 없습니다.");
        }
    }

    private LocalDateTime resolveFromInclusive(WorkScheduleInquiryRequestDto request, LocalDateTime now) {
        if (request.getYear() == now.getYear() && request.getMonth() == now.getMonthValue()) {
            return now;
        }
        return LocalDateTime.of(request.getYear(), request.getMonth(), 1, 0, 0);
    }

    private List<WorkspaceShift> fetchShifts(
        AppActor actor,
        Workspace workspace,
        WorkScheduleInquiryRequestDto request,
        LocalDateTime fromInclusive
    ) {
        return workspaceShiftQueryRepository.findByUserAndWorkspaceAndMonthFrom(
            actor.getUser(), workspace, request.getYear(), request.getMonth(), fromInclusive
        );
    }
}
