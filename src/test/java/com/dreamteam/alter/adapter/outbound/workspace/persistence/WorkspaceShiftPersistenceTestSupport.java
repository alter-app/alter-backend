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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 근무(WorkspaceShift) 영속성 테스트 공통 준비. 사용자·업장·근무자·확정 근무를 저장하는 헬퍼를 제공한다.
 */
@DataJpaTest
@Import({
    QueryDslConfig.class,
    WorkspaceQueryRepositoryImpl.class,
    WorkspaceShiftQueryRepositoryImpl.class,
    WorkspaceShiftRepositoryImpl.class,
    WorkspaceWorkerRepositoryImpl.class,
    UserRepositoryImpl.class,
    ManagerUserRepositoryImpl.class,
    WorkspaceRepositoryImpl.class,
    BusinessTypeRepositoryImpl.class
})
abstract class WorkspaceShiftPersistenceTestSupport {

    @Autowired
    protected WorkspaceQueryRepositoryImpl workspaceQueryRepository;

    @Autowired
    protected WorkspaceShiftQueryRepositoryImpl workspaceShiftQueryRepository;

    @Autowired
    protected WorkspaceShiftRepositoryImpl workspaceShiftRepository;

    @Autowired
    protected WorkspaceWorkerRepositoryImpl workspaceWorkerRepository;

    @Autowired
    protected UserRepositoryImpl userRepository;

    @Autowired
    protected ManagerUserRepositoryImpl managerUserRepository;

    @Autowired
    protected WorkspaceRepositoryImpl workspaceRepository;

    @Autowired
    protected BusinessTypeRepositoryImpl businessTypeRepository;

    protected User saveUser() {
        User user = User.create(
            "010" + String.valueOf(System.nanoTime()).substring(0, 8), "encoded", "김알바",
            "nickname" + System.nanoTime(), UserGender.GENDER_MALE, "19990101",
            "user" + System.nanoTime() + "@example.com"
        );
        return userRepository.save(user);
    }

    protected Workspace saveWorkspace() {
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

    protected WorkspaceWorker saveWorker(Workspace workspace, User user) {
        WorkspaceWorker worker = WorkspaceWorker.create(workspace, user);
        workspaceWorkerRepository.save(worker);
        return worker;
    }

    protected WorkspaceShift saveConfirmedShift(WorkspaceWorker worker, LocalDateTime start, LocalDateTime end) {
        WorkspaceShift shift = WorkspaceShift.create(worker.getWorkspace(), start, end, "홀", WorkspaceShiftStatus.CONFIRMED);
        shift.assignWorker(worker);
        return workspaceShiftRepository.save(shift);
    }
}
