package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceShiftQueryRepositoryImplTests extends WorkspaceShiftPersistenceTestSupport {

    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 5, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 10, 5, 18, 0);

    @Test
    void hasConflictingSchedule_타_업장의_CONFIRMED_근무와_겹치면_true() {
        User user = saveUser();
        WorkspaceWorker workerA = saveWorker(saveWorkspace(), user);
        WorkspaceWorker workerB = saveWorker(saveWorkspace(), user);
        saveConfirmedShift(workerB, START.plusHours(4), END.plusHours(4));

        boolean result = workspaceShiftQueryRepository.hasConflictingSchedule(workerA, START, END);

        assertThat(result).isTrue();
    }

    @Test
    void hasConflictingSchedule_타_업장_RESIGNED_근무자의_잔여_근무도_겹침으로_본다() {
        User user = saveUser();
        WorkspaceWorker workerA = saveWorker(saveWorkspace(), user);
        WorkspaceWorker resignedWorker = saveWorker(saveWorkspace(), user);
        saveConfirmedShift(resignedWorker, START, END);
        resignedWorker.resign();
        workspaceWorkerRepository.save(resignedWorker);

        boolean result = workspaceShiftQueryRepository.hasConflictingSchedule(workerA, START, END);

        assertThat(result).isTrue();
    }

    @Test
    void hasConflictingSchedule_다른_사용자의_근무는_겹침이_아니다() {
        Workspace workspace = saveWorkspace();
        WorkspaceWorker worker = saveWorker(workspace, saveUser());
        WorkspaceWorker otherWorker = saveWorker(workspace, saveUser());
        saveConfirmedShift(otherWorker, START, END);

        boolean result = workspaceShiftQueryRepository.hasConflictingSchedule(worker, START, END);

        assertThat(result).isFalse();
    }

    @Test
    void hasConflictingSchedule_excludeShiftId로_자기_자신은_제외한다() {
        WorkspaceWorker worker = saveWorker(saveWorkspace(), saveUser());
        WorkspaceShift self = saveConfirmedShift(worker, START, END);

        boolean selfOnly = workspaceShiftQueryRepository.hasConflictingSchedule(
            worker, START.plusHours(1), END.plusHours(1), self.getId());
        boolean withoutExclude = workspaceShiftQueryRepository.hasConflictingSchedule(
            worker, START.plusHours(1), END.plusHours(1), null);

        assertThat(selfOnly).isFalse();
        assertThat(withoutExclude).isTrue();
    }

    @Test
    void hasConflictingSchedule_excludeShiftId가_있어도_다른_근무와_겹치면_true() {
        WorkspaceWorker worker = saveWorker(saveWorkspace(), saveUser());
        WorkspaceShift self = saveConfirmedShift(worker, START, END);
        saveConfirmedShift(worker, END, END.plusHours(3));

        boolean result = workspaceShiftQueryRepository.hasConflictingSchedule(
            worker, START.plusHours(1), END.plusHours(1), self.getId());

        assertThat(result).isTrue();
    }

    @Test
    void hasConflictingSchedule_끝과_시작이_맞닿은_인접_근무는_겹침이_아니다() {
        WorkspaceWorker worker = saveWorker(saveWorkspace(), saveUser());
        saveConfirmedShift(worker, START, END);

        boolean after = workspaceShiftQueryRepository.hasConflictingSchedule(worker, END, END.plusHours(3));
        boolean before = workspaceShiftQueryRepository.hasConflictingSchedule(worker, START.minusHours(3), START);

        assertThat(after).isFalse();
        assertThat(before).isFalse();
    }

    @Test
    void hasConflictingSchedule_CONFIRMED가_아닌_근무는_무시한다() {
        WorkspaceWorker worker = saveWorker(saveWorkspace(), saveUser());
        WorkspaceShift cancelled = saveConfirmedShift(worker, START, END);
        cancelled.unassignWorker();
        workspaceShiftRepository.save(cancelled);
        workspaceShiftRepository.save(
            WorkspaceShift.create(worker.getWorkspace(), START, END, "홀", WorkspaceShiftStatus.PLANNED));

        boolean result = workspaceShiftQueryRepository.hasConflictingSchedule(worker, START, END);

        assertThat(result).isFalse();
    }

    @Test
    void findConfirmedByUserIdsAndDateRange_타_업장_근무도_포함한다() {
        User user = saveUser();
        WorkspaceWorker workerA = saveWorker(saveWorkspace(), user);
        WorkspaceWorker workerB = saveWorker(saveWorkspace(), user);
        WorkspaceShift shiftA = saveConfirmedShift(workerA, START, END);
        WorkspaceShift shiftB = saveConfirmedShift(workerB, START.plusDays(1), END.plusDays(1));
        saveConfirmedShift(saveWorker(saveWorkspace(), saveUser()), START, END);

        List<WorkspaceShift> result = workspaceShiftQueryRepository.findConfirmedByUserIdsAndDateRange(
            List.of(user.getId()), START.minusDays(1), END.plusDays(7));

        assertThat(result).extracting(WorkspaceShift::getId)
            .containsExactlyInAnyOrder(shiftA.getId(), shiftB.getId());
    }
}
