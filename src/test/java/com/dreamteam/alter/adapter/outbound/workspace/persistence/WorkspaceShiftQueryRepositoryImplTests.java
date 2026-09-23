package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.outbound.user.persistence.ManagerUserRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.user.persistence.UserRepositoryImpl;
import com.dreamteam.alter.common.config.QueryDslConfig;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.type.ManagerUserStatus;
import com.dreamteam.alter.domain.user.type.UserGender;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    QueryDslConfig.class,
    WorkspaceShiftQueryRepositoryImpl.class,
    WorkspaceShiftRepositoryImpl.class,
    WorkspaceWorkerRepositoryImpl.class,
    UserRepositoryImpl.class,
    ManagerUserRepositoryImpl.class,
    WorkspaceRepositoryImpl.class,
    BusinessTypeRepositoryImpl.class
})
class WorkspaceShiftQueryRepositoryImplTests {

    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 5, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 10, 5, 18, 0);

    @Autowired
    private WorkspaceShiftQueryRepositoryImpl workspaceShiftQueryRepository;

    @Autowired
    private WorkspaceShiftRepositoryImpl workspaceShiftRepository;

    @Autowired
    private WorkspaceWorkerRepositoryImpl workspaceWorkerRepository;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private ManagerUserRepositoryImpl managerUserRepository;

    @Autowired
    private WorkspaceRepositoryImpl workspaceRepository;

    @Autowired
    private BusinessTypeRepositoryImpl businessTypeRepository;

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

    private User saveUser() {
        User user = User.create(
            "010" + String.valueOf(System.nanoTime()).substring(0, 8), "encoded", "김알바",
            "nickname" + System.nanoTime(), UserGender.GENDER_MALE, "19990101",
            "user" + System.nanoTime() + "@example.com"
        );
        return userRepository.save(user);
    }

    private Workspace saveWorkspace() {
        ManagerUser managerUser = managerUserRepository.save(ManagerUser.create(saveUser(), ManagerUserStatus.ACTIVATED));
        BusinessType businessType = businessTypeRepository.save(
            BusinessType.create("업종" + System.nanoTime(), null));
        Workspace workspace = Workspace.create(
            managerUser, "000-00-00000", "사장님가게", businessType, null,
            "01000000000", "설명", WorkspaceStatus.ACTIVATED, "서울시 강남구",
            "서울특별시", "강남구", "역삼동", BigDecimal.ONE, BigDecimal.ONE
        );
        workspaceRepository.save(workspace);
        return workspace;
    }

    private WorkspaceWorker saveWorker(Workspace workspace, User user) {
        WorkspaceWorker worker = WorkspaceWorker.create(workspace, user);
        workspaceWorkerRepository.save(worker);
        return worker;
    }

    private WorkspaceShift saveConfirmedShift(WorkspaceWorker worker, LocalDateTime start, LocalDateTime end) {
        WorkspaceShift shift = WorkspaceShift.create(worker.getWorkspace(), start, end, "홀", WorkspaceShiftStatus.CONFIRMED);
        shift.assignWorker(worker);
        return workspaceShiftRepository.save(shift);
    }
}
