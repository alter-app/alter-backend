package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ApproveSubstituteRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.UpdateWorkScheduleRequestDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.UserQueryRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.SubstituteRequestQueryRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.WorkspaceShiftQueryRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.WorkspaceShiftRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.WorkspaceWorkerQueryRepositoryImpl;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.config.QueryDslConfig;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.exception.handler.GlobalExceptionHandler;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.type.ManagerUserStatus;
import com.dreamteam.alter.domain.user.type.UserGender;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestType;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;

@Testcontainers
@DataJpaTest(showSql = false, properties = {
    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.hibernate.ddl-auto=create", "spring.jpa.show-sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(PersistenceExceptionTranslationAutoConfiguration.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import({QueryDslConfig.class, UserQueryRepositoryImpl.class, WorkspaceShiftQueryRepositoryImpl.class,
    WorkspaceShiftRepositoryImpl.class, WorkspaceWorkerQueryRepositoryImpl.class, SubstituteRequestQueryRepositoryImpl.class,
    ManagerAssignWorkerToSchedule.class, ManagerUpdateWorkerInSchedule.class, ManagerUpdateWorkSchedule.class,
    ManagerApproveSubstituteRequest.class, GenerateNextMonthWorkspaceShiftTx.class})
class WorkspaceShiftConcurrencyTests {
    @Container static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2");
    private static final LocalDateTime START = LocalDateTime.of(2030, 1, 7, 9, 0);
    private static final LocalDateTime END = START.plusHours(3);

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", postgres::getJdbcUrl);
        properties.add("spring.datasource.username", postgres::getUsername);
        properties.add("spring.datasource.password", postgres::getPassword);
        properties.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired ManagerAssignWorkerToSchedule assign;
    @Autowired ManagerUpdateWorkerInSchedule replace;
    @Autowired ManagerUpdateWorkSchedule changeTime;
    @Autowired ManagerApproveSubstituteRequest approve;
    @Autowired GenerateNextMonthWorkspaceShiftTx generate;
    @Autowired UserQueryRepository users;
    @MockitoBean NotificationService notifications;
    @MockitoSpyBean WorkspaceShiftQueryRepositoryImpl shifts;
    @MockitoSpyBean SubstituteRequestQueryRepositoryImpl requests;

    enum Scenario { ASSIGN, OTHER_WORKSPACE, SUBSTITUTE, BATCH }
    enum LockTarget { USER, SHIFT }
    enum Mutation { ASSIGN, REPLACE, TIME }
    record Fixture(Long managerId, Long workspaceId, Long otherWorkspaceId, Long userId,
                   Long workerId, Long otherWorkspaceWorkerId, Long otherWorkerId,
                   Long firstShiftId, Long secondShiftId, Long requestId) {}

    @AfterEach
    void clearSpy() { reset(shifts, requests); }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    void concurrentConfirmation_rechecksLatestUserSchedule(Scenario scenario) throws Exception {
        Fixture fixture = fixture(scenario);
        CountDownLatch firstChecked = new CountDownLatch(1);
        CountDownLatch secondCheckedOrDone = new CountDownLatch(1);
        AtomicInteger checks = new AtomicInteger();
        doAnswer(invocation -> {
            boolean result = (boolean) invocation.callRealMethod();
            if (checks.incrementAndGet() == 1) {
                firstChecked.countDown();
                secondCheckedOrDone.await(1, TimeUnit.SECONDS);
            } else {
                secondCheckedOrDone.countDown();
            }
            return result;
        }).when(shifts).hasConflictingSchedule(any(), any(), any(), nullable(Long.class));

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> attempt(() -> tx().executeWithoutResult(status ->
                assign.execute(actor(fixture), fixture.firstShiftId(), fixture.workerId()))));
            assertThat(firstChecked.await(5, TimeUnit.SECONDS)).isTrue();
            var second = executor.submit(() -> {
                try { return attempt(() -> secondAction(fixture, scenario)); }
                finally { secondCheckedOrDone.countDown(); }
            });
            first.get(15, TimeUnit.SECONDS);
            second.get(15, TimeUnit.SECONDS);
        }

        tx().executeWithoutResult(status -> assertThat(em.createQuery(
            "select count(s) from WorkspaceShift s where s.assignedWorkspaceWorker.user.id = :user " +
                "and s.status = :confirmed and s.startDateTime < :end and s.endDateTime > :start", Long.class)
            .setParameter("user", fixture.userId()).setParameter("confirmed", WorkspaceShiftStatus.CONFIRMED)
            .setParameter("start", START).setParameter("end", END).getSingleResult()).isEqualTo(1));
    }

    @Test
    void approval_readsUpdatedShiftThroughLazyRequestAssociation() throws Exception {
        Fixture fixture = fixture(Scenario.SUBSTITUTE);
        tx().executeWithoutResult(status -> {
            WorkspaceShift conflict = em.find(WorkspaceShift.class, fixture.firstShiftId());
            conflict.update(END, END.plusHours(3), "기존");
            conflict.assignWorker(em.find(WorkspaceWorker.class, fixture.workerId()));
        });
        CountDownLatch requestLoaded = new CountDownLatch(1);
        CountDownLatch timeUpdated = new CountDownLatch(1);
        doAnswer(invocation -> {
            Object result = invocation.callRealMethod();
            requestLoaded.countDown();
            assertThat(timeUpdated.await(5, TimeUnit.SECONDS)).isTrue();
            return result;
        }).when(requests).findById(fixture.requestId());
        try (var executor = Executors.newFixedThreadPool(2)) {
            var approval = executor.submit(() -> attempt(() -> tx().executeWithoutResult(status ->
                approve.execute(actor(fixture), fixture.requestId(), new ApproveSubstituteRequestDto("승인")))));
            assertThat(requestLoaded.await(5, TimeUnit.SECONDS)).isTrue();
            var update = executor.submit(() -> {
                try { tx().executeWithoutResult(status -> changeTime.execute(actor(fixture), fixture.secondShiftId(),
                    new UpdateWorkScheduleRequestDto(END, END.plusHours(3), "수정"))); }
                finally { timeUpdated.countDown(); }
            });
            update.get(10, TimeUnit.SECONDS);
            assertThat(approval.get(10, TimeUnit.SECONDS)).isEqualTo(ErrorCode.CONFLICT);
        }
        tx().executeWithoutResult(status -> {
            assertThat(em.find(WorkspaceShift.class, fixture.secondShiftId()).getStartDateTime()).isEqualTo(END);
            assertThat(em.find(SubstituteRequest.class, fixture.requestId()).getStatus()).isEqualTo(SubstituteRequestStatus.ACCEPTED);
        });
    }

    @ParameterizedTest
    @EnumSource(Mutation.class)
    void manualMutation_keepsDeletedShiftNotFound(Mutation mutation) {
        Fixture fixture = fixture(Scenario.ASSIGN);
        tx().executeWithoutResult(status -> em.find(WorkspaceShift.class, fixture.firstShiftId()).delete());
        assertThatThrownBy(() -> tx().executeWithoutResult(status -> {
            switch (mutation) {
                case ASSIGN -> assign.execute(actor(fixture), fixture.firstShiftId(), fixture.workerId());
                case REPLACE -> replace.execute(actor(fixture), fixture.firstShiftId(), fixture.otherWorkerId());
                case TIME -> changeTime.execute(actor(fixture), fixture.firstShiftId(), new UpdateWorkScheduleRequestDto(START, END, "수정"));
            }
        })).isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void forUpdateReadsDeletedShiftWithoutChangingItsState() {
        Fixture fixture = fixture(Scenario.SUBSTITUTE);
        tx().executeWithoutResult(status -> em.find(WorkspaceShift.class, fixture.secondShiftId()).delete());
        tx().executeWithoutResult(status -> {
            WorkspaceShift shift = shifts.findByIdForUpdate(fixture.secondShiftId()).orElseThrow();
            assertThat(shift.getId()).isEqualTo(fixture.secondShiftId());
            assertThat(shift.getStatus()).isEqualTo(WorkspaceShiftStatus.DELETED);
            assertThat(em.find(SubstituteRequest.class, fixture.requestId()).getStatus()).isEqualTo(SubstituteRequestStatus.ACCEPTED);
        });
    }

    @Test
    void userLocks_areOrderedEvenWithReverseAndDuplicateInput() throws Exception {
        Fixture fixture = fixture(Scenario.ASSIGN);
        Long secondUser = tx().execute(status -> em.find(WorkspaceWorker.class, fixture.otherWorkerId()).getUser().getId());
        CountDownLatch firstLocked = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        List<Long> expected = List.of(fixture.userId(), secondUser).stream().sorted().toList();
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> tx().execute(status -> {
                List<User> result = users.findAllByIdForUpdate(List.of(secondUser, fixture.userId(), secondUser));
                firstLocked.countDown();
                try { assertThat(secondStarted.await(5, TimeUnit.SECONDS)).isTrue(); }
                catch (InterruptedException exception) { throw new RuntimeException(exception); }
                return result.stream().map(User::getId).toList();
            }));
            assertThat(firstLocked.await(5, TimeUnit.SECONDS)).isTrue();
            var second = executor.submit(() -> {
                secondStarted.countDown();
                return tx().execute(status -> users.findAllByIdForUpdate(List.of(fixture.userId(), secondUser))
                    .stream().map(User::getId).toList());
            });
            assertThat(first.get(10, TimeUnit.SECONDS)).isEqualTo(expected);
            assertThat(second.get(10, TimeUnit.SECONDS)).isEqualTo(expected);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void touchingAndSeparatedSchedulesRemainAssignable(int gapHours) {
        Fixture fixture = fixture(Scenario.ASSIGN);
        tx().executeWithoutResult(status -> em.find(WorkspaceShift.class, fixture.secondShiftId())
            .update(END.plusHours(gapHours), END.plusHours(gapHours + 3), "다음"));
        tx().executeWithoutResult(status -> assign.execute(actor(fixture), fixture.firstShiftId(), fixture.workerId()));
        tx().executeWithoutResult(status -> assign.execute(actor(fixture), fixture.secondShiftId(), fixture.workerId()));
        tx().executeWithoutResult(status -> {
            assertThat(em.find(WorkspaceShift.class, fixture.firstShiftId()).getStatus()).isEqualTo(WorkspaceShiftStatus.CONFIRMED);
            assertThat(em.find(WorkspaceShift.class, fixture.secondShiftId()).getStatus()).isEqualTo(WorkspaceShiftStatus.CONFIRMED);
        });
    }

    @Test
    void updatingAssignedShiftStillExcludesItself() {
        Fixture fixture = fixture(Scenario.ASSIGN);
        tx().executeWithoutResult(status -> assign.execute(actor(fixture), fixture.firstShiftId(), fixture.workerId()));
        tx().executeWithoutResult(status -> changeTime.execute(actor(fixture), fixture.firstShiftId(),
            new UpdateWorkScheduleRequestDto(START, END, "수정")));
        tx().executeWithoutResult(status -> assertThat(em.find(WorkspaceShift.class, fixture.firstShiftId()).getPosition())
            .isEqualTo("수정"));
    }

    @ParameterizedTest
    @EnumSource(LockTarget.class)
    void lockTimeoutRetainsExisting429ResponseAndDoesNotAssign(LockTarget target) throws Exception {
        Fixture fixture = fixture(Scenario.ASSIGN);
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try (var executor = Executors.newSingleThreadExecutor()) {
            var holder = executor.submit(() -> tx().executeWithoutResult(status -> {
                if (target == LockTarget.USER) users.findAllByIdForUpdate(List.of(fixture.userId()));
                else shifts.findByIdForUpdate(fixture.firstShiftId());
                locked.countDown();
                try { assertThat(release.await(15, TimeUnit.SECONDS)).isTrue(); }
                catch (InterruptedException exception) { throw new RuntimeException(exception); }
            }));
            assertThat(locked.await(5, TimeUnit.SECONDS)).isTrue();
            try {
                PessimisticLockingFailureException failure = org.assertj.core.api.Assertions.catchThrowableOfType(
                    () -> tx().executeWithoutResult(status -> assign.execute(actor(fixture), fixture.firstShiftId(), fixture.workerId())),
                    PessimisticLockingFailureException.class);
                assertThat(failure).isNotNull();
                var response = new GlobalExceptionHandler().handlePessimisticLockingFailureException(failure);
                assertThat(response.getStatusCode().value()).isEqualTo(429);
                assertThat(response.getBody().code()).isEqualTo("E001");
            } finally { release.countDown(); }
            holder.get(5, TimeUnit.SECONDS);
        }
        tx().executeWithoutResult(status -> assertThat(em.find(WorkspaceShift.class, fixture.firstShiftId()).getAssignedWorkspaceWorker())
            .isNull());
    }

    @Test
    void replacement_waitingForTimeUpdateUsesLatestShiftTime() throws Exception {
        Fixture fixture = fixture(Scenario.SUBSTITUTE);
        tx().executeWithoutResult(status -> {
            em.find(WorkspaceShift.class, fixture.secondShiftId()).delete();
            WorkspaceShift shift = em.find(WorkspaceShift.class, fixture.firstShiftId());
            shift.assignWorker(em.find(WorkspaceWorker.class, fixture.workerId()));
            WorkspaceShift conflict = WorkspaceShift.create(shift.getWorkspace(), END, END.plusHours(3), "홀", WorkspaceShiftStatus.PLANNED);
            conflict.assignWorker(em.find(WorkspaceWorker.class, fixture.otherWorkerId()));
            em.persist(conflict);
        });
        CountDownLatch updateChecked = new CountDownLatch(1);
        CountDownLatch replacementLoaded = new CountDownLatch(1);
        CountDownLatch updateDone = new CountDownLatch(1);
        doAnswer(invocation -> {
            boolean result = (boolean) invocation.callRealMethod();
            WorkspaceWorker worker = invocation.getArgument(0);
            if (worker.getUser().getId().equals(fixture.userId())) {
                updateChecked.countDown();
                replacementLoaded.await(1, TimeUnit.SECONDS);
            }
            return result;
        }).when(shifts).hasConflictingSchedule(any(), any(), any(), nullable(Long.class));
        doAnswer(invocation -> {
            Object result = invocation.callRealMethod();
            if (Thread.currentThread().getName().equals("replacement")) {
                replacementLoaded.countDown();
                assertThat(updateDone.await(5, TimeUnit.SECONDS)).isTrue();
            }
            return result;
        }).when(shifts).findById(fixture.firstShiftId());

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> {
                try { tx().executeWithoutResult(status -> changeTime.execute(actor(fixture), fixture.firstShiftId(),
                    new UpdateWorkScheduleRequestDto(END, END.plusHours(3), "수정"))); }
                finally { updateDone.countDown(); }
            });
            assertThat(updateChecked.await(5, TimeUnit.SECONDS)).isTrue();
            var second = executor.submit(() -> {
                Thread.currentThread().setName("replacement");
                return attempt(() -> tx().executeWithoutResult(status ->
                    replace.execute(actor(fixture), fixture.firstShiftId(), fixture.otherWorkerId())));
            });
            first.get(15, TimeUnit.SECONDS);
            assertThat(second.get(15, TimeUnit.SECONDS)).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
        }
        tx().executeWithoutResult(status -> {
            WorkspaceShift result = em.find(WorkspaceShift.class, fixture.firstShiftId());
            assertThat(result.getStartDateTime()).isEqualTo(END);
            assertThat(result.getAssignedWorkspaceWorker().getUser().getId()).isEqualTo(fixture.userId());
        });
    }

    private void secondAction(Fixture fixture, Scenario scenario) {
        if (scenario == Scenario.BATCH) {
            tx().executeWithoutResult(status -> {
                WorkspaceWorker worker = em.find(WorkspaceWorker.class, fixture.workerId());
                WorkspaceWorkerSchedule schedule = WorkspaceWorkerSchedule.create(worker, START.getDayOfWeek(),
                    START.toLocalTime(), START.getDayOfWeek(), END.toLocalTime());
                generate.execute(worker.getWorkspace(), List.of(schedule), YearMonth.from(START), Map.of());
            });
        } else {
            tx().executeWithoutResult(status -> {
                if (scenario == Scenario.SUBSTITUTE) {
                    approve.execute(actor(fixture), fixture.requestId(), new ApproveSubstituteRequestDto("승인"));
                } else {
                    assign.execute(actor(fixture), fixture.secondShiftId(), scenario == Scenario.OTHER_WORKSPACE
                        ? fixture.otherWorkspaceWorkerId() : fixture.workerId());
                }
            });
        }
    }

    private ErrorCode attempt(Runnable action) {
        try { action.run(); return null; }
        catch (CustomException exception) {
            assertThat(exception.getErrorCode()).isIn(ErrorCode.ILLEGAL_ARGUMENT, ErrorCode.CONFLICT);
            return exception.getErrorCode();
        }
    }

    private ManagerActor actor(Fixture fixture) {
        return ManagerActor.from(em.find(ManagerUser.class, fixture.managerId()), List.of());
    }

    private Fixture fixture(Scenario scenario) {
        return tx().execute(status -> {
            User user = user();
            User other = user();
            ManagerUser manager = ManagerUser.create(user(), ManagerUserStatus.ACTIVATED);
            em.persist(manager);
            BusinessType business = BusinessType.create("업종" + System.nanoTime(), null);
            em.persist(business);
            Workspace workspace = workspace(manager, business);
            Workspace otherWorkspace = workspace(manager, business);
            WorkspaceWorker worker = worker(workspace, user);
            WorkspaceWorker otherWorker = worker(workspace, other);
            WorkspaceWorker secondWorkspaceWorker = worker(otherWorkspace, user);
            WorkspaceShift first = shift(workspace);
            WorkspaceShift second = shift(scenario == Scenario.OTHER_WORKSPACE ? otherWorkspace : workspace);
            SubstituteRequest request = null;
            if (scenario == Scenario.SUBSTITUTE) {
                second.assignWorker(otherWorker);
                request = SubstituteRequest.create(second, otherWorker.getId(), SubstituteRequestType.SPECIFIC, "교환");
                request.accept(worker.getId());
                em.persist(request);
            }
            return new Fixture(manager.getId(), workspace.getId(), otherWorkspace.getId(), user.getId(), worker.getId(),
                secondWorkspaceWorker.getId(), otherWorker.getId(), first.getId(), second.getId(), request == null ? null : request.getId());
        });
    }

    private User user() {
        String unique = String.valueOf(System.nanoTime());
        User user = User.create("010" + unique.substring(0, 8), "encoded", "테스트", "user" + unique,
            UserGender.GENDER_MALE, "19990101", unique + "@example.com");
        em.persist(user);
        return user;
    }

    private Workspace workspace(ManagerUser manager, BusinessType business) {
        Workspace workspace = Workspace.create(manager, "000-00-00000", "가게", business, null,
            "01000000000", "설명", WorkspaceStatus.ACTIVATED, "서울시 강남구", "서울특별시", "강남구", "역삼동",
            BigDecimal.ONE, BigDecimal.ONE);
        em.persist(workspace);
        return workspace;
    }

    private WorkspaceWorker worker(Workspace workspace, User user) {
        WorkspaceWorker worker = WorkspaceWorker.create(workspace, user);
        em.persist(worker);
        return worker;
    }

    private WorkspaceShift shift(Workspace workspace) {
        WorkspaceShift shift = WorkspaceShift.create(workspace, START, END, "홀", WorkspaceShiftStatus.PLANNED);
        em.persist(shift);
        return shift;
    }

    private TransactionTemplate tx() { return new TransactionTemplate(transactionManager); }
}
