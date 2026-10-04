package com.dreamteam.alter.application.workspace.usecase;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("generateNextMonthWorkspaceShiftTx")
@RequiredArgsConstructor
public class GenerateNextMonthWorkspaceShiftTx {

    // TODO: 포지션 삭제 시 제거 예정
    private static final String DEFAULT_POSITION = "고정 근무";

    private final WorkspaceShiftRepository workspaceShiftRepository;

    /**
     * existingShiftsByUserId 는 읽기만 한다. 이 Tx는 REQUIRES_NEW 로 따로 롤백될 수 있으므로
     * 생성한 근무는 결과로 돌려주고, 호출 측이 성공한 뒤에만 공유 맵에 합친다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GenerationResult execute(
        Workspace workspace,
        List<WorkspaceWorkerSchedule> schedules,
        YearMonth targetMonth,
        Map<Long, List<WorkspaceShift>> existingShiftsByUserId
    ) {
        if (schedules.isEmpty()) {
            return new GenerationResult(List.of(), 0);
        }

        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();

        List<WorkspaceShift> shiftsToCreate = new ArrayList<>();
        // 이 호출에서 생성한 근무. 같은 업장 안에서 뒤 스케줄의 겹침 판정에 쓴다.
        Map<Long, List<WorkspaceShift>> createdByUserId = new HashMap<>();
        int skipped = 0;

        for (WorkspaceWorkerSchedule schedule : schedules) {
            LocalDate firstStartDate = calculateFirstOccurrence(startDate, schedule.getStartDayOfWeek());
            if (firstStartDate.isAfter(endDate)) {
                continue;
            }

            WorkspaceWorker workspaceWorker = schedule.getWorkspaceWorker();
            Long userId = workspaceWorker.getUser().getId();
            List<WorkspaceShift> existingShifts = existingShiftsByUserId.getOrDefault(userId, List.of());
            List<WorkspaceShift> createdShifts = createdByUserId.computeIfAbsent(userId, k -> new ArrayList<>());

            for (LocalDate currentStartDate = firstStartDate;
                 !currentStartDate.isAfter(endDate);
                 currentStartDate = currentStartDate.plusWeeks(1)) {

                LocalDateTime startDateTime = currentStartDate.atTime(schedule.getStartTime());
                LocalDateTime endDateTime = currentStartDate
                    .plusDays(calculateDayOffset(schedule.getStartDayOfWeek(), schedule.getEndDayOfWeek()))
                    .atTime(schedule.getEndTime());

                if (hasConflict(existingShifts, startDateTime, endDateTime)
                    || hasConflict(createdShifts, startDateTime, endDateTime)) {
                    skipped++;
                    log.info("[고정 근무 생성] 겹침으로 건너뜀 - userId={}, workspaceId={}, start={}, end={}",
                        userId, workspace.getId(), startDateTime, endDateTime);
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
                createdShifts.add(shift);
            }
        }

        if (!shiftsToCreate.isEmpty()) {
            workspaceShiftRepository.saveAll(shiftsToCreate);
        }

        return new GenerationResult(shiftsToCreate, skipped);
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

    public record GenerationResult(List<WorkspaceShift> createdShifts, int skipped) {
        public int created() {
            return createdShifts.size();
        }
    }
}
