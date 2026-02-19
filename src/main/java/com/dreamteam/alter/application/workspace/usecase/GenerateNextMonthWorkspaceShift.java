package com.dreamteam.alter.application.workspace.usecase;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.inbound.GenerateNextMonthWorkspaceShiftUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("generateNextMonthWorkspaceShift")
@RequiredArgsConstructor
@Transactional
public class GenerateNextMonthWorkspaceShift implements GenerateNextMonthWorkspaceShiftUseCase {

    private static final String DEFAULT_POSITION = "고정 근무";

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;
    private final WorkspaceShiftRepository workspaceShiftRepository;
    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;

    @Override
    public void execute() {
        // 서비스 타임존 기준으로 오늘 날짜의 day-of-month를 구한다
        LocalDate now = LocalDate.now();
        int todayDayOfMonth = now.getDayOfMonth();

        // 오늘이 고정 근무 생성일로 설정된 워크스페이스만 조회한다
        List<Workspace> targetWorkspaces = workspaceQueryRepository.findAllByNextMonthShiftGenDay(todayDayOfMonth);

        if (targetWorkspaces.isEmpty()) {
            log.info("[고정 근무 생성] 오늘({})에 해당하는 대상 워크스페이스가 없습니다.", now);
            return;
        }

        List<Long> workspaceIds = targetWorkspaces.stream()
            .map(Workspace::getId)
            .toList();

        log.info("[고정 근무 생성] 대상 워크스페이스 {}개 조회 완료.", workspaceIds.size());

        // 대상 워크스페이스들의 활성화된 고정 스케줄을 한 번에 조회 후 워크스페이스별로 그룹화한다
        List<WorkspaceWorkerSchedule> allSchedules = workspaceWorkerScheduleQueryRepository
            .findAllActivatedWithWorkspaceWorkerByWorkspaceIds(workspaceIds);

        Map<Long, List<WorkspaceWorkerSchedule>> schedulesByWorkspaceId = allSchedules.stream()
            .collect(Collectors.groupingBy(
                schedule -> schedule.getWorkspaceWorker().getWorkspace().getId()
            ));

        YearMonth targetMonth = YearMonth.from(now).plusMonths(1);
        int totalCreated = 0;
        int totalSkipped = 0;
        int failedWorkspaceCount = 0;

        for (Workspace workspace : targetWorkspaces) {
            try {
                List<WorkspaceWorkerSchedule> schedules = schedulesByWorkspaceId
                    .getOrDefault(workspace.getId(), List.of());

                if (schedules.isEmpty()) {
                    log.info("[고정 근무 생성] 워크스페이스({}) - 활성화된 고정 스케줄 없음. 건너뜀.", workspace.getId());
                    continue;
                }

                GenerationResult result = generateShiftsForWorkspace(workspace, schedules, targetMonth);
                totalCreated += result.created;
                totalSkipped += result.skipped;

                log.info("[고정 근무 생성] 워크스페이스({}) - 생성: {}건, 충돌 건너뜀: {}건", workspace.getId(), result.created, result.skipped);
            } catch (Exception e) {
                failedWorkspaceCount++;
                log.error("[고정 근무 생성] 워크스페이스({}) 처리 중 오류 발생: {}", workspace.getId(), e.getMessage(), e);
            }
        }

        log.info("[고정 근무 생성] 배치 완료 - 총 생성: {}건, 총 건너뜀: {}건, 실패 워크스페이스: {}개", totalCreated, totalSkipped, failedWorkspaceCount);
    }

    /**
     * 단일 워크스페이스에 대해 다음 달 고정 근무 시프트를 생성한다.
     */
    private GenerationResult generateShiftsForWorkspace(
        Workspace workspace,
        List<WorkspaceWorkerSchedule> schedules,
        YearMonth targetMonth
    ) {
        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();

        List<WorkspaceShift> shiftsToSave = new ArrayList<>();
        int skipped = 0;

        for (WorkspaceWorkerSchedule schedule : schedules) {
            // 해당 스케줄의 요일을 만족하는 다음 달 첫 날짜를 계산한다
            LocalDate firstStartDate = calculateFirstOccurrence(startDate, schedule.getStartDayOfWeek());
            if (firstStartDate.isAfter(endDate)) {
                continue;
            }

            // 매주 반복하면서 시프트를 생성한다
            for (LocalDate currentStartDate = firstStartDate;
                 !currentStartDate.isAfter(endDate);
                 currentStartDate = currentStartDate.plusWeeks(1)) {

                WorkspaceWorker workspaceWorker = schedule.getWorkspaceWorker();
                LocalDateTime startDateTime = currentStartDate.atTime(schedule.getStartTime());
                LocalDateTime endDateTime = currentStartDate
                    .plusDays(calculateDayOffset(schedule.getStartDayOfWeek(), schedule.getEndDayOfWeek()))
                    .atTime(schedule.getEndTime());

                // 이미 확정된 근무와 겹치면 자동 생성하지 않는다
                if (workspaceShiftQueryRepository.hasConflictingSchedule(workspaceWorker, startDateTime, endDateTime)) {
                    skipped++;
                    continue;
                }

                WorkspaceShift shift = WorkspaceShift.create(
                    workspace,
                    startDateTime,
                    endDateTime,
                    DEFAULT_POSITION,
                    WorkspaceShiftStatus.CONFIRMED
                );
                shift.assignWorker(workspaceWorker);
                shiftsToSave.add(shift);
            }
        }

        if (!shiftsToSave.isEmpty()) {
            workspaceShiftRepository.saveAll(shiftsToSave);
        }

        return new GenerationResult(shiftsToSave.size(), skipped);
    }

    // 해당 요일이 처음으로 등장하는 날짜를 반환한다
    private LocalDate calculateFirstOccurrence(LocalDate monthStart, DayOfWeek targetDay) {
        int diff = targetDay.getValue() - monthStart.getDayOfWeek().getValue();
        if (diff < 0) {
            diff += 7;
        }
        return monthStart.plusDays(diff);
    }

    // 시작 요일 대비 종료 요일까지 필요한 일수 오프셋을 계산한다
    private long calculateDayOffset(DayOfWeek startDay, DayOfWeek endDay) {
        long offset = endDay.getValue() - startDay.getValue();
        // 종료 요일이 시작 요일보다 이른 경우 (예: 금요일 시작 → 월요일 종료) 다음 주로 넘어간다
        if (offset < 0) {
            offset += 7;
        }
        return offset;
    }

    private record GenerationResult(int created, int skipped) {}
}
