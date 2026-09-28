package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.GetMyScheduleResponseDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MyScheduleResponseDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MyWorkspaceWorkSummaryDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkScheduleInquiryRequestDto;
import com.dreamteam.alter.common.constants.WorkspaceConstants;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.port.inbound.GetMyScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("getMySchedule")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMySchedule implements GetMyScheduleUseCase {

    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;

    @Override
    public GetMyScheduleResponseDto execute(AppActor actor, WorkScheduleInquiryRequestDto request) {

        List<WorkspaceShift> shifts;
        // 근무시간·예상 급여 집계 대상 (월·일 조회 시 해당 월 전체)
        List<WorkspaceShift> monthlyShifts = null;

        if (ObjectUtils.isNotEmpty(request.getYear()) && ObjectUtils.isNotEmpty(request.getMonth()) && ObjectUtils.isNotEmpty(request.getDay())) {
            // 1. 년/월/일 포함 -> 일별 조회
            shifts = workspaceShiftQueryRepository.findByUserAndDate(
                    actor.getUser(),
                    request.getYear(),
                    request.getMonth(),
                    request.getDay()
            );
            monthlyShifts = workspaceShiftQueryRepository.findByUserAndDateRange(
                    actor.getUser(),
                    request.getYear(),
                    request.getMonth()
            );

        } else if (ObjectUtils.isNotEmpty(request.getYear()) && ObjectUtils.isNotEmpty(request.getMonth())) {
            // 2. 년/월 포함 -> 월별 조회
            shifts = workspaceShiftQueryRepository.findByUserAndDateRange(
                    actor.getUser(),
                    request.getYear(),
                    request.getMonth()
            );
            monthlyShifts = shifts;

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

        if (monthlyShifts == null) {
            // 이번 주 조회는 근무시간만 제공
            double weeklyWorkHours = shifts.stream()
                .mapToDouble(WorkspaceShift::getWorkHours)
                .sum();
            return GetMyScheduleResponseDto.of(weeklyWorkHours, null, null, scheduleDtos);
        }

        // 업장 상세와 같은 방식으로 업장별 예상 급여를 계산하고, 합계는 업장별 금액의 합으로 맞춘다
        Map<Workspace, Double> workHoursByWorkspace = monthlyShifts.stream()
            .collect(Collectors.groupingBy(
                WorkspaceShift::getWorkspace,
                LinkedHashMap::new,
                Collectors.summingDouble(WorkspaceShift::getWorkHours)
            ));

        List<MyWorkspaceWorkSummaryDto> workspaceSummaries = workHoursByWorkspace.entrySet().stream()
            .map(entry -> MyWorkspaceWorkSummaryDto.of(
                entry.getKey(),
                entry.getValue(),
                Math.round(entry.getValue() * WorkspaceConstants.MINIMUM_HOURLY_WAGE)
            ))
            .toList();

        double totalWorkHours = workspaceSummaries.stream()
            .mapToDouble(MyWorkspaceWorkSummaryDto::getTotalWorkHours)
            .sum();
        long estimatedSalary = workspaceSummaries.stream()
            .mapToLong(MyWorkspaceWorkSummaryDto::getEstimatedSalary)
            .sum();

        return GetMyScheduleResponseDto.of(totalWorkHours, estimatedSalary, workspaceSummaries, scheduleDtos);
    }
}
