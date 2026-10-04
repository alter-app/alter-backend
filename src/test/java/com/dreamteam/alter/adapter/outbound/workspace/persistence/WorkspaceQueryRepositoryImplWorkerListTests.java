package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceWorkerListFilterDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.ManagerUserRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.user.persistence.UserRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceWorkerListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceWorkerListResponse;
import com.dreamteam.alter.common.config.QueryDslConfig;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.type.ManagerUserStatus;
import com.dreamteam.alter.domain.user.type.UserGender;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import jakarta.persistence.EntityManager;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Autowired
    private EntityManager entityManager;

    private static final AtomicInteger SEQ = new AtomicInteger();

    private User saveUser() {
        return saveUser("김알바");
    }

    private User saveUser(String name) {
        User user = User.create(
            String.format("010%08d", SEQ.incrementAndGet()), "encoded", name,
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
        long count = workspaceQueryRepository.getWorkspaceWorkerCount(managerUser, workspace.getId(), filter);

        assertThat(result).extracting(ManagerWorkspaceWorkerListResponse::getId)
            .containsExactly(resigned.getId());
        assertThat(result).extracting(r -> r.getStatus())
            .containsExactly(WorkspaceWorkerStatus.RESIGNED);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void getWorkspaceWorkerListWithCursor_status_미지정_퇴사일필터만_있으면_퇴사자_조회() {
        ManagerUser managerUser = saveManagerUser();
        Workspace workspace = saveWorkspace(managerUser);
        saveWorker(workspace, saveUser());
        WorkspaceWorker resigned = saveWorker(workspace, saveUser());
        resigned.resign();
        workspaceWorkerRepository.save(resigned);

        ManagerWorkspaceWorkerListFilterDto filter = new ManagerWorkspaceWorkerListFilterDto(
            null, null, null, null, LocalDate.now().minusDays(1), null
        );

        List<ManagerWorkspaceWorkerListResponse> result = workspaceQueryRepository
            .getWorkspaceWorkerListWithCursor(managerUser, workspace.getId(), filter, firstPage());
        long count = workspaceQueryRepository.getWorkspaceWorkerCount(managerUser, workspace.getId(), filter);

        assertThat(result).extracting(ManagerWorkspaceWorkerListResponse::getId)
            .containsExactly(resigned.getId());
        assertThat(result).extracting(r -> r.getStatus())
            .containsExactly(WorkspaceWorkerStatus.RESIGNED);
        assertThat(count).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"manager", "app", "exchangeable"})
    void workerCursorFollowsNameThenIdInsteadOfCreationTime(String path) {
        ManagerUser manager = saveManagerUser();
        Workspace workspace = saveWorkspace(manager);
        WorkspaceWorker beta = saveWorker(workspace, saveUser("Beta"));
        WorkspaceWorker alphaFirst = saveWorker(workspace, saveUser("Alpha"));
        WorkspaceWorker alphaSecond = saveWorker(workspace, saveUser("Alpha"));
        WorkspaceWorker resigned = saveWorker(workspace, saveUser("A resigned"));
        resigned.resign();
        workspaceWorkerRepository.save(resigned);
        entityManager.flush();
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);
        for (WorkspaceWorker worker : List.of(beta, alphaFirst, alphaSecond)) {
            entityManager.createNativeQuery("UPDATE workspace_workers SET created_at = :time WHERE id = :id")
                .setParameter("time", base.plusDays(worker.getId()))
                .setParameter("id", worker.getId()).executeUpdate();
        }
        entityManager.clear();

        List<Long> actual = new ArrayList<>();
        CursorDto cursor = null;
        for (int page = 0; page < 3; page++) {
            var result = workerPage(path, manager, workspace.getId(), cursor);
            assertThat(result).hasSize(1);
            actual.add(result.getFirst().getId());
            cursor = new CursorDto(result.getFirst().getId(), result.getFirst().getCreatedAt());
        }
        assertThat(actual).containsExactly(alphaFirst.getId(), alphaSecond.getId(), beta.getId());
        assertThat(workerPage(path, manager, workspace.getId(), cursor)).isEmpty();
        assertThat(workspaceQueryRepository.getWorkspaceWorkerCount(manager, workspace.getId(), defaultFilter())).isEqualTo(3);
        assertThat(workspaceQueryRepository.getUserWorkspaceWorkerCount(workspace.getId())).isEqualTo(3);
        assertThat(workspaceQueryRepository.getExchangeableWorkerCount(workspace.getId(), null,
            base, base.plusHours(1))).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(strings = {"manager", "app", "exchangeable"})
    void workerCursorCannotUseAnchorFromAnotherWorkspace(String path) {
        ManagerUser manager = saveManagerUser();
        Workspace workspace = saveWorkspace(manager);
        saveWorker(workspace, saveUser("Beta"));
        WorkspaceWorker other = saveWorker(saveWorkspace(manager), saveUser("Alpha"));
        assertThat(workerPage(path, manager, workspace.getId(),
            new CursorDto(other.getId(), other.getCreatedAt().plusDays(1)))).isEmpty();
    }

    @Test
    void managerWorkerListAndCountRemainScopedToOwnerAndNameFilter() {
        ManagerUser manager = saveManagerUser();
        Workspace workspace = saveWorkspace(manager);
        WorkspaceWorker alpha = saveWorker(workspace, saveUser("Alpha"));
        saveWorker(workspace, saveUser("Beta"));
        var filter = new ManagerWorkspaceWorkerListFilterDto(null, "Alpha", null, null, null, null);
        assertThat(workspaceQueryRepository.getWorkspaceWorkerListWithCursor(manager, workspace.getId(), filter, firstPage()))
            .extracting(ManagerWorkspaceWorkerListResponse::getId).containsExactly(alpha.getId());
        assertThat(workspaceQueryRepository.getWorkspaceWorkerCount(manager, workspace.getId(), filter)).isEqualTo(1);
        ManagerUser other = saveManagerUser();
        assertThat(workspaceQueryRepository.getWorkspaceWorkerListWithCursor(other, workspace.getId(), filter, firstPage())).isEmpty();
        assertThat(workspaceQueryRepository.getWorkspaceWorkerCount(other, workspace.getId(), filter)).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"manager", "app", "exchangeable"})
    void cursorMissingRequiredFieldsIsInvalidCursor(String path) {
        ManagerUser manager = saveManagerUser();
        Workspace workspace = saveWorkspace(manager);
        WorkspaceWorker worker = saveWorker(workspace, saveUser("Alpha"));
        for (CursorDto cursor : List.of(new CursorDto(worker.getId(), null),
            new CursorDto(null, worker.getCreatedAt()), new CursorDto(null, null))) {
            assertThatThrownBy(() -> workerPage(path, manager, workspace.getId(), cursor))
                .isInstanceOfSatisfying(CustomException.class,
                    exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURSOR));
        }
    }

    private ManagerWorkspaceWorkerListFilterDto defaultFilter() {
        return new ManagerWorkspaceWorkerListFilterDto(null, null, null, null, null, null);
    }

    private List<UserWorkspaceWorkerListResponse> workerPage(
        String path, ManagerUser manager, Long workspaceId, CursorDto cursor
    ) {
        var request = CursorPageRequest.of(cursor, 1);
        if (path.equals("manager")) {
            return workspaceQueryRepository.getWorkspaceWorkerListWithCursor(manager, workspaceId, defaultFilter(), request)
                .stream().map(row -> new UserWorkspaceWorkerListResponse(
                    row.getId(), null, null, null, null, row.getCreatedAt())).toList();
        }
        if (path.equals("app")) return workspaceQueryRepository.getUserWorkspaceWorkerListWithCursor(workspaceId, request);
        return workspaceQueryRepository.getExchangeableWorkerListWithCursor(workspaceId, null,
            LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 1, 1, 0), request);
    }

}
