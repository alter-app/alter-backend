package com.dreamteam.alter.application.workspace.usecase;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;

import lombok.RequiredArgsConstructor;

@Service("generateNextMonthWorkspaceShiftTx")
@RequiredArgsConstructor
public class GenerateNextMonthWorkspaceShiftTx {

    // TODO: 포지션 삭제 시 제거 예정
    private static final String DEFAULT_POSITION = "고정 근무";

    private final WorkspaceShiftRepository workspaceShiftRepository;

    /**
     * existingShiftsByUserId 는 가변 맵이어야 한다 — 이번 실행에서 생성한 근무를 사용자별 목록에 추가해 뒤 업장 처리에 반영한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GenerationResult execute(
        Workspace workspace,
        List<WorkspaceWorkerSchedule> schedules,
        YearMonth targetMonth,
        Map<Long, List<WorkspaceShift>> existingShiftsByUserId
    ) {
        if (schedules.isEmpty()) {
            return new GenerationResult(0, 0);
        }

        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();

        List<WorkspaceShift> shiftsToCreate = new ArrayList<>();
        int skipped = 0;

        for (WorkspaceWorkerSchedule schedule : schedules) {
            LocalDate firstStartDate = calculateFirstOccurrence(startDate, schedule.getStartDayOfWeek());
            if (firstStartDate.isAfter(endDate)) {
                continue;
            }

            WorkspaceWorker workspaceWorker = schedule.getWorkspaceWorker();
            List<WorkspaceShift> existingShifts =
                existingShiftsByUserId.computeIfAbsent(workspaceWorker.getUser().getId(), k -> new ArrayList<>());

            for (LocalDate currentStartDate = firstStartDate;
                 !currentStartDate.isAfter(endDate);
                 currentStartDate = currentStartDate.plusWeeks(1)) {

                LocalDateTime startDateTime = currentStartDate.atTime(schedule.getStartTime());
                LocalDateTime endDateTime = currentStartDate
                    .plusDays(calculateDayOffset(schedule.getStartDayOfWeek(), schedule.getEndDayOfWeek()))
                    .atTime(schedule.getEndTime());

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
                // 같은 배치에서 뒤에 처리되는 다른 업장의 고정 근무가 이 근무와 겹치지 않도록 즉시 반영한다
                existingShifts.add(shift);
            }
        }

        if (!shiftsToCreate.isEmpty()) {
            workspaceShiftRepository.saveAll(shiftsToCreate);
        }

        return new GenerationResult(shiftsToCreate.size(), skipped);
    }

    private boolean hasConflict(List<WorkspaceShift> existingShifts, LocalDateTime start, LocalDateTime end) {
        for (WorkspaceShift existing : existingShifts) {
            if (existing.getStartDateTime().isBefore(end) && existing.getEndDateTime().isAfter(start)) {
                return true;
            }
        }
        return false;
    }

    private LocalDate calculateFirstOccurrence(LocalDate monthStart, DayOfWeek targetDay) {
        int diff = targetDay.getValue() - monthStart.getDayOfWeek().getValue();
        if (diff < 0) {
            diff += 7;
        }
        return monthStart.plusDays(diff);
    }

    private long calculateDayOffset(DayOfWeek startDay, DayOfWeek endDay) {
        long offset = endDay.getValue() - startDay.getValue();
        if (offset < 0) {
            offset += 7;
        }
        return offset;
    }

    public record GenerationResult(int created, int skipped) {}
}
