package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceWorkerListFilterDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.ManagerUserRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.user.persistence.UserRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceWorkerListResponse;
import com.dreamteam.alter.common.config.QueryDslConfig;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.type.ManagerUserStatus;
import com.dreamteam.alter.domain.user.type.UserGender;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    QueryDslConfig.class,
    WorkspaceQueryRepositoryImpl.class,
    WorkspaceWorkerRepositoryImpl.class,
    WorkspaceRepositoryImpl.class,
    UserRepositoryImpl.class,
    ManagerUserRepositoryImpl.class,
    BusinessTypeRepositoryImpl.class
})
class WorkspaceQueryRepositoryImplWorkerListTests {

    @Autowired
    private WorkspaceQueryRepositoryImpl workspaceQueryRepository;

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

    private User saveUser() {
        User user = User.create(
            "010" + String.valueOf(System.nanoTime()).substring(0, 8), "encoded", "김알바",
            "nickname" + System.nanoTime(), UserGender.GENDER_MALE, "19990101",
            "user" + System.nanoTime() + "@example.com"
        );
        return userRepository.save(user);
    }

    private ManagerUser saveManagerUser() {
        User user = saveUser();
        return managerUserRepository.save(ManagerUser.create(user, ManagerUserStatus.ACTIVATED));
    }

    private Workspace saveWorkspace(ManagerUser managerUser) {
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

    private CursorPageRequest<CursorDto> firstPage() {
        return CursorPageRequest.of(null, 20);
    }

    @Test
    void getWorkspaceWorkerListWithCursor_status_미지정시_RESIGNED_근무자_제외() {
        ManagerUser managerUser = saveManagerUser();
        Workspace workspace = saveWorkspace(managerUser);
        WorkspaceWorker activated = saveWorker(workspace, saveUser());
        WorkspaceWorker resigned = saveWorker(workspace, saveUser());
        resigned.resign();
        workspaceWorkerRepository.save(resigned);

        ManagerWorkspaceWorkerListFilterDto filter = new ManagerWorkspaceWorkerListFilterDto(
            null, null, null, null, null, null
        );

        List<ManagerWorkspaceWorkerListResponse> result = workspaceQueryRepository
            .getWorkspaceWorkerListWithCursor(managerUser, workspace.getId(), filter, firstPage());
        long count = workspaceQueryRepository.getWorkspaceWorkerCount(managerUser, workspace.getId(), filter);

        assertThat(result).extracting(ManagerWorkspaceWorkerListResponse::getId)
            .containsExactly(activated.getId());
        assertThat(result).extracting(r -> r.getStatus())
            .containsExactly(WorkspaceWorkerStatus.ACTIVATED);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void getWorkspaceWorkerListWithCursor_status_RESIGNED_지정시_퇴사자만_조회() {
        ManagerUser managerUser = saveManagerUser();
        Workspace workspace = saveWorkspace(managerUser);
        saveWorker(workspace, saveUser());
        WorkspaceWorker resigned = saveWorker(workspace, saveUser());
        resigned.resign();
        workspaceWorkerRepository.save(resigned);

        ManagerWorkspaceWorkerListFilterDto filter = new ManagerWorkspaceWorkerListFilterDto(
            WorkspaceWorkerStatus.RESIGNED, null, null, null, null, null
        );

        List<ManagerWorkspaceWorkerListResponse> result = workspaceQueryRepository
            .getWorkspaceWorkerListWithCursor(managerUser, workspace.getId(), filter, firstPage());

        assertThat(result).extracting(ManagerWorkspaceWorkerListResponse::getId)
            .containsExactly(resigned.getId());
        assertThat(result).extracting(r -> r.getStatus())
            .containsExactly(WorkspaceWorkerStatus.RESIGNED);
    }

}
