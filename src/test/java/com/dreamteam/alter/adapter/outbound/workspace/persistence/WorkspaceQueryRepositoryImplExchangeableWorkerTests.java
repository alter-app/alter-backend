package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.user.persistence.ManagerUserRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.user.persistence.UserRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceWorkerListResponse;
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
    WorkspaceQueryRepositoryImpl.class,
    WorkspaceShiftRepositoryImpl.class,
    WorkspaceWorkerRepositoryImpl.class,
    UserRepositoryImpl.class,
    ManagerUserRepositoryImpl.class,
    WorkspaceRepositoryImpl.class,
    BusinessTypeRepositoryImpl.class
})
class WorkspaceQueryRepositoryImplExchangeableWorkerTests {

    private static final LocalDateTime START = LocalDateTime.of(2099, 10, 5, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2099, 10, 5, 18, 0);

    @Autowired
    private WorkspaceQueryRepositoryImpl workspaceQueryRepository;

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
    void 교환_후보_조회는_타_업장_근무와_겹치는_근무자를_제외한다() {
        Workspace workspace = saveWorkspace();
        User requester = saveUser();
        saveWorker(workspace, requester);

        User busyElsewhere = saveUser();
        WorkspaceWorker busyWorker = saveWorker(workspace, busyElsewhere);
        saveConfirmedShift(saveWorker(saveWorkspace(), busyElsewhere), START.plusHours(2), END.plusHours(2));

        WorkspaceWorker freeWorker = saveWorker(workspace, saveUser());

        List<Long> ids = workspaceQueryRepository.getExchangeableWorkerIds(workspace.getId(), requester, START, END);
        long count = workspaceQueryRepository.getExchangeableWorkerCount(workspace.getId(), requester, START, END);
        List<UserWorkspaceWorkerListResponse> page = workspaceQueryRepository.getExchangeableWorkerListWithCursor(
            workspace.getId(), requester, START, END, CursorPageRequest.of((CursorDto) null, 20));

        assertThat(ids).containsExactly(freeWorker.getId()).doesNotContain(busyWorker.getId());
        assertThat(count).isEqualTo(1);
        assertThat(page).extracting(UserWorkspaceWorkerListResponse::getId).containsExactly(freeWorker.getId());
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
