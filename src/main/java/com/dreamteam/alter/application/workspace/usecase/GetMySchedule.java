package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.GetMyScheduleResponseDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MyScheduleResponseDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkScheduleInquiryRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.port.inbound.GetMyScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.dreamteam.alter.domain.workspace.result.WorkSummaryResult;
import com.dreamteam.alter.domain.workspace.result.WorkspaceWorkSummaryResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service("getMySchedule")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMySchedule implements GetMyScheduleUseCase {

    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;

    @Override
    public GetMyScheduleResponseDto execute(AppActor actor, WorkScheduleInquiryRequestDto request) {

        List<WorkspaceShift> shifts;
        boolean isMonthlyQuery = false;

        if (ObjectUtils.isNotEmpty(request.getYear()) && ObjectUtils.isNotEmpty(request.getMonth()) && ObjectUtils.isNotEmpty(request.getDay())) {
            // 1. 년/월/일 포함 -> 일별 조회
            shifts = workspaceShiftQueryRepository.findByUserAndDate(
                    actor.getUser(),
                    toDate(request)
            );

        } else if (ObjectUtils.isNotEmpty(request.getYear()) && ObjectUtils.isNotEmpty(request.getMonth())) {
            // 2. 년/월 포함 -> 월별 조회
            shifts = workspaceShiftQueryRepository.findByUserAndDateRange(
                    actor.getUser(),
                    request.getYear(),
                    request.getMonth()
            );
            isMonthlyQuery = true;

        } else if (ObjectUtils.isEmpty(request.getYear()) && ObjectUtils.isEmpty(request.getMonth()) && ObjectUtils.isEmpty(request.getDay())) {
            // 3. 인자 없음 -> 이번 주 스케줄 조회 (월~일)
            LocalDate now = LocalDate.now();
            LocalDate startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            shifts = workspaceShiftQueryRepository.findByUserAndWeeklyRange(
                    actor.getUser(),
                    startOfWeek,
                    endOfWeek
            );

        } else {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "연단위 요청은 불가능합니다.");
        }

        List<MyScheduleResponseDto> scheduleDtos = shifts.stream()
            .map(MyScheduleResponseDto::of)
            .toList();

        if (!isMonthlyQuery) {
            // 일·주 조회는 근무시간만 제공
            return GetMyScheduleResponseDto.of(WorkSummaryResult.from(shifts).totalWorkHours(), scheduleDtos);
        }

        // 업장 상세와 같은 방식으로 업장별 예상 급여를 계산하고, 합계는 업장별 금액의 합으로 맞춘다 (업장 ID 오름차순)
        List<WorkspaceWorkSummaryResult> workspaceSummaries = shifts.stream()
            .collect(Collectors.groupingBy(shift -> shift.getWorkspace().getId(), TreeMap::new, Collectors.toList()))
            .values().stream()
            .map(workspaceShifts -> WorkspaceWorkSummaryResult.of(workspaceShifts.get(0).getWorkspace(), workspaceShifts))
            .toList();

        double totalWorkHours = workspaceSummaries.stream()
            .mapToDouble(WorkspaceWorkSummaryResult::totalWorkHours)
            .sum();
        long estimatedSalary = workspaceSummaries.stream()
            .mapToLong(WorkspaceWorkSummaryResult::estimatedSalary)
            .sum();

        return GetMyScheduleResponseDto.of(totalWorkHours, estimatedSalary, workspaceSummaries, scheduleDtos);
    }

    private LocalDate toDate(WorkScheduleInquiryRequestDto request) {
        try {
            return LocalDate.of(request.getYear(), request.getMonth(), request.getDay());
        } catch (DateTimeException e) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "존재하지 않는 날짜입니다.");
        }
    }
}
