package com.dreamteam.alter.application.workspace.usecase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.inbound.GenerateNextMonthWorkspaceShiftUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("generateNextMonthWorkspaceShift")
@RequiredArgsConstructor
public class GenerateNextMonthWorkspaceShift implements GenerateNextMonthWorkspaceShiftUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;
    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;
    private final GenerateNextMonthWorkspaceShiftTx generateNextMonthWorkspaceShiftTx;

    @Override
    public void execute() {
        LocalDate now = LocalDate.now();
        int todayDayOfMonth = now.getDayOfMonth();
        int lastDayOfMonth = now.lengthOfMonth();
        boolean isLastDayOfMonth = todayDayOfMonth == lastDayOfMonth;

        // 말일이면 생성일이 오늘 이후인 워크스페이스까지 포함한다.
        List<Workspace> targetWorkspaces = workspaceQueryRepository.findAllForNextMonthShiftGeneration(
            todayDayOfMonth,
            isLastDayOfMonth
        );

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

        int totalCreated = 0;
        int totalSkipped = 0;
        int failedWorkspaceCount = 0;

        for (Workspace workspace : targetWorkspaces) {
            try {
                List<WorkspaceWorkerSchedule> schedules = schedulesByWorkspaceId
                    .getOrDefault(workspace.getId(), List.of());

                if (schedules.isEmpty()) {
                    continue;
                }

                GenerateNextMonthWorkspaceShiftTx.GenerationResult result = generateNextMonthWorkspaceShiftTx.execute(
                    workspace,
                    schedules,
                    targetMonth,
                    existingShiftsByWorkerId
                );
                totalCreated += result.created();
                totalSkipped += result.skipped();
            } catch (Exception e) {
                failedWorkspaceCount++;
                log.error("[고정 근무 생성] 워크스페이스({}) 처리 중 오류 발생: {}", workspace.getId(), e.getMessage(), e);
            }
        }

        log.info("[고정 근무 생성] 배치 완료 - 총 생성: {}건, 총 건너뜀: {}건, 실패 워크스페이스: {}개", totalCreated, totalSkipped, failedWorkspaceCount);
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

}
