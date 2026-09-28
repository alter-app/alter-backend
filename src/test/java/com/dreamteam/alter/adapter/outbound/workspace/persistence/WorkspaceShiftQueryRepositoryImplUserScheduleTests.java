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
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    QueryDslConfig.class,
    WorkspaceShiftQueryRepositoryImpl.class,
    WorkspaceShiftRepositoryImpl.class,
    WorkspaceWorkerRepositoryImpl.class,
    WorkspaceRepositoryImpl.class,
    UserRepositoryImpl.class,
    ManagerUserRepositoryImpl.class,
    BusinessTypeRepositoryImpl.class
})
class WorkspaceShiftQueryRepositoryImplUserScheduleTests {

    @Autowired
    private WorkspaceShiftQueryRepositoryImpl workspaceShiftQueryRepository;

    @Autowired
    private WorkspaceShiftRepositoryImpl workspaceShiftRepository;

    @Autowired
    private WorkspaceWorkerRepositoryImpl workspaceWorkerRepository;

    @Autowired
    private WorkspaceRepositoryImpl workspaceRepository;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private ManagerUserRepositoryImpl managerUserRepository;

    @Autowired
    private BusinessTypeRepositoryImpl businessTypeRepository;

    private static final AtomicInteger SEQ = new AtomicInteger();

    private User saveUser() {
        User user = User.create(
            String.format("010%08d", SEQ.incrementAndGet()), "encoded", "김알바",
            "nickname" + System.nanoTime(), UserGender.GENDER_MALE, "19990101",
            "user" + System.nanoTime() + "@example.com"
        );
        return userRepository.save(user);
    }

    private Workspace saveWorkspace() {
        ManagerUser managerUser = managerUserRepository.save(ManagerUser.create(saveUser(), ManagerUserStatus.ACTIVATED));
        BusinessType businessType = businessTypeRepository.save(BusinessType.create("업종" + System.nanoTime(), null));
        Workspace workspace = Workspace.create(
            managerUser, "000-00-00000", "사장님가게", businessType, null,
            "01000000000", "설명", WorkspaceStatus.ACTIVATED, "서울시 강남구",
            "서울특별시", "강남구", "역삼동", BigDecimal.ONE, BigDecimal.ONE
        );
        workspaceRepository.save(workspace);
        return workspace;
    }

    private WorkspaceShift saveAssignedShift(Workspace workspace, WorkspaceWorker worker, LocalDateTime start) {
        WorkspaceShift shift = WorkspaceShift.create(workspace, start, start.plusHours(4), "홀", WorkspaceShiftStatus.CONFIRMED);
        shift.assignWorker(worker);
        return workspaceShiftRepository.save(shift);
    }

    @Test
    void findByUserAndDate_사장님이_삭제한_근무_제외() {
        Workspace workspace = saveWorkspace();
        User user = saveUser();
        WorkspaceWorker worker = WorkspaceWorker.create(workspace, user);
        workspaceWorkerRepository.save(worker);

        WorkspaceShift confirmed = saveAssignedShift(workspace, worker, LocalDateTime.of(2026, 9, 2, 9, 0));
        WorkspaceShift deleted = saveAssignedShift(workspace, worker, LocalDateTime.of(2026, 9, 2, 15, 0));
        deleted.delete();
        workspaceShiftRepository.save(deleted);

        assertThat(workspaceShiftQueryRepository.findByUserAndDate(user, 2026, 9, 2))
            .extracting(WorkspaceShift::getId)
            .containsExactly(confirmed.getId());
    }
}
