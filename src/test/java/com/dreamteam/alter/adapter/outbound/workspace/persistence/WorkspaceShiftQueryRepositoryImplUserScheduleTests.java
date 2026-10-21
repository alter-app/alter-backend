package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceShiftQueryRepositoryImplUserScheduleTests extends WorkspaceShiftPersistenceTestSupport {

    @Autowired
    private TestEntityManager entityManager;

    private WorkspaceShift saveShift(WorkspaceWorker worker, LocalDateTime start) {
        return saveConfirmedShift(worker, start, start.plusHours(4));
    }

    @Test
    void findByUserAndDate_사장님이_삭제한_근무_제외() {
        User user = saveUser();
        WorkspaceWorker worker = saveWorker(saveWorkspace(), user);

        WorkspaceShift confirmed = saveShift(worker, LocalDateTime.of(2026, 9, 2, 9, 0));
        WorkspaceShift deleted = saveShift(worker, LocalDateTime.of(2026, 9, 2, 15, 0));
        deleted.delete();
        workspaceShiftRepository.save(deleted);

        assertThat(workspaceShiftQueryRepository.findByUserAndDate(user, LocalDate.of(2026, 9, 2)))
            .extracting(WorkspaceShift::getId)
            .containsExactly(confirmed.getId());
    }

    @Test
    void findByUserAndWeeklyRange_종료일인_일요일_근무_포함() {
        User user = saveUser();
        WorkspaceWorker worker = saveWorker(saveWorkspace(), user);

        WorkspaceShift monday = saveShift(worker, LocalDateTime.of(2026, 9, 28, 0, 0));
        WorkspaceShift sunday = saveShift(worker, LocalDateTime.of(2026, 10, 4, 18, 0));
        saveShift(worker, LocalDateTime.of(2026, 10, 5, 0, 0));

        assertThat(workspaceShiftQueryRepository.findByUserAndWeeklyRange(user, LocalDate.of(2026, 9, 28), LocalDate.of(2026, 10, 4)))
            .extracting(WorkspaceShift::getId)
            .containsExactly(monday.getId(), sunday.getId());
    }

    @Test
    void 사용자_근무_조회는_업장을_함께_조회해_업장별_추가_쿼리_없음() {
        User user = saveUser();
        Workspace cafe = saveWorkspace();
        Workspace store = saveWorkspace();
        saveShift(saveWorker(cafe, user), LocalDateTime.of(2026, 9, 1, 9, 0));
        saveShift(saveWorker(store, user), LocalDateTime.of(2026, 9, 2, 9, 0));
        entityManager.flush();
        entityManager.clear();

        List<List<WorkspaceShift>> results = List.of(
            workspaceShiftQueryRepository.findByUserAndDateRange(user, 2026, 9),
            workspaceShiftQueryRepository.findByUserAndWeeklyRange(user, LocalDate.of(2026, 8, 31), LocalDate.of(2026, 9, 6)),
            workspaceShiftQueryRepository.findByUserAndDate(user, LocalDate.of(2026, 9, 1))
        );

        assertThat(results).extracting(List::size).containsExactly(2, 2, 1);
        assertThat(results).flatMap(shifts -> shifts)
            .allSatisfy(shift -> assertThat(Hibernate.isInitialized(shift.getWorkspace())).isTrue());
    }
}
