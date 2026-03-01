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

    // TODO: 포지션 삭제 시 제거 예정
    private static final String DEFAULT_POSITION = "고정 근무";

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;
    private final WorkspaceShiftRepository workspaceShiftRepository;
    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;

    @Override
    public void execute() {
        LocalDate now = LocalDate.now();
        int todayDayOfMonth = now.getDayOfMonth();

        // 오늘이 고정 근무 생성일로 설정된 워크스페이스만 조회한다
        List<Workspace> targetWorkspaces = workspaceQueryRepository.findAllByNextMonthShiftGenDay(todayDayOfMonth);

        if (targetWorkspaces.isEmpty()) {
            return;
        }

        List<Long> workspaceIds = targetWorkspaces.stream()
            .map(Workspace::getId)
            .toList();

        // 대상 워크스페이스들의 활성화된 고정 스케줄을 한 번에 조회 후 워크스페이스별로 그룹화한다
        List<WorkspaceWorkerSchedule> allSchedules = workspaceWorkerScheduleQueryRepository
            .findAllActivatedWithWorkspaceWorkerByWorkspaceIds(workspaceIds);

        Map<Long, List<WorkspaceWorkerSchedule>> schedulesByWorkspaceId = allSchedules.stream()
            .collect(Collectors.groupingBy(
                schedule -> schedule.getWorkspaceWorker().getWorkspace().getId()
            ));

        YearMonth targetMonth = YearMonth.from(now).plusMonths(1);

        Map<Long, List<WorkspaceShift>> existingShiftsByWorkerId = buildExistingShiftsMap(allSchedules, targetMonth);

        List<WorkspaceShift> allShiftsToSave = new ArrayList<>();
        int totalSkipped = 0;
        int failedWorkspaceCount = 0;

        for (Workspace workspace : targetWorkspaces) {
            try {
                List<WorkspaceWorkerSchedule> schedules = schedulesByWorkspaceId
                    .getOrDefault(workspace.getId(), List.of());

                if (schedules.isEmpty()) {
                    continue;
                }

                GenerationResult result = generateShiftsForWorkspace(workspace, schedules, targetMonth, existingShiftsByWorkerId);
                allShiftsToSave.addAll(result.shifts());
                totalSkipped += result.skipped();
            } catch (Exception e) {
                failedWorkspaceCount++;
                log.error("[고정 근무 생성] 워크스페이스({}) 처리 중 오류 발생: {}", workspace.getId(), e.getMessage(), e);
            }
        }

        if (!allShiftsToSave.isEmpty()) {
            workspaceShiftRepository.saveAll(allShiftsToSave);
        }

        log.info("[고정 근무 생성] 배치 완료 - 총 생성: {}건, 총 건너뜀: {}건, 실패 워크스페이스: {}개", allShiftsToSave.size(), totalSkipped, failedWorkspaceCount);
    }

    /**
     * 고정 스케줄의 종료 시간이 월말을 최대 6일 넘을 수 있으므로 조회 범위에 여유를 둔다.
     */
    private Map<Long, List<WorkspaceShift>> buildExistingShiftsMap(
        List<WorkspaceWorkerSchedule> allSchedules, YearMonth targetMonth) {
        List<Long> workerIds = allSchedules.stream()
            .map(s -> s.getWorkspaceWorker().getId())
            .distinct()
            .toList();

        if (workerIds.isEmpty()) {
            return Map.of();
        }

        LocalDateTime from = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime to = targetMonth.atEndOfMonth().plusDays(7).atStartOfDay();

        return workspaceShiftQueryRepository
            .findConfirmedByWorkerIdsAndDateRange(workerIds, from, to)
            .stream()
            .collect(Collectors.groupingBy(
                shift -> shift.getAssignedWorkspaceWorker().getId()
            ));
    }

    /**
     * 단일 워크스페이스에 대해 다음 달 고정 근무 시프트를 생성한다.
     */
    private GenerationResult generateShiftsForWorkspace(
        Workspace workspace,
        List<WorkspaceWorkerSchedule> schedules,
        YearMonth targetMonth,
        Map<Long, List<WorkspaceShift>> existingShiftsByWorkerId
    ) {
        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();

        List<WorkspaceShift> shiftsToCreate = new ArrayList<>();
        int skipped = 0;

        for (WorkspaceWorkerSchedule schedule : schedules) {
            // 해당 스케줄의 요일을 만족하는 다음 달 첫 날짜를 계산한다
            LocalDate firstStartDate = calculateFirstOccurrence(startDate, schedule.getStartDayOfWeek());
            if (firstStartDate.isAfter(endDate)) {
                continue;
            }

            WorkspaceWorker workspaceWorker = schedule.getWorkspaceWorker();
            List<WorkspaceShift> existingShifts = existingShiftsByWorkerId
                .getOrDefault(workspaceWorker.getId(), List.of());

            // 매주 반복하면서 시프트를 생성한다
            for (LocalDate currentStartDate = firstStartDate;
                 !currentStartDate.isAfter(endDate);
                 currentStartDate = currentStartDate.plusWeeks(1)) {

                LocalDateTime startDateTime = currentStartDate.atTime(schedule.getStartTime());
                LocalDateTime endDateTime = currentStartDate
                    .plusDays(calculateDayOffset(schedule.getStartDayOfWeek(), schedule.getEndDayOfWeek()))
                    .atTime(schedule.getEndTime());

                // 이미 확정된 근무와 겹치면 자동 생성하지 않는다
                if (hasConflict(existingShifts, startDateTime, endDateTime)) {
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
                shiftsToCreate.add(shift);
            }
        }

        return new GenerationResult(shiftsToCreate, skipped);
    }

    // 인메모리 충돌 여부 판단: 기존 시프트 목록과 시간 범위가 겹치는지 확인한다
    private boolean hasConflict(List<WorkspaceShift> existingShifts, LocalDateTime start, LocalDateTime end) {
        for (WorkspaceShift existing : existingShifts) {
            if (existing.getStartDateTime().isBefore(end) && existing.getEndDateTime().isAfter(start)) {
                return true;
            }
        }
        return false;
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

    private record GenerationResult(List<WorkspaceShift> shifts, int skipped) {}
}
