package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.UpdateUserPostingApplicationStatusRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingApplicationStatusRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingStatusRequestDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.PostingApplicationQueryRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.posting.persistence.PostingQueryRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.WorkspaceWorkerQueryRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.WorkspaceWorkerRepositoryImpl;
import com.dreamteam.alter.application.user.usecase.UpdateUserPostingApplicationStatus;
import com.dreamteam.alter.application.workspace.usecase.AddWorkerToWorkspace;
import com.dreamteam.alter.common.config.QueryDslConfig;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.command.CreatePostingCommand;
import com.dreamteam.alter.domain.posting.command.PostingScheduleCommand;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.type.ManagerUserStatus;
import com.dreamteam.alter.domain.user.type.UserGender;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@DataJpaTest(showSql = false, properties = {
    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.hibernate.ddl-auto=create",
    "spring.jpa.show-sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import({QueryDslConfig.class, PostingQueryRepositoryImpl.class, PostingApplicationQueryRepositoryImpl.class,
    UpdateUserPostingApplicationStatus.class, ManagerUpdatePostingApplicationStatus.class, ManagerUpdatePostingStatus.class,
    AddWorkerToWorkspace.class, WorkspaceWorkerRepositoryImpl.class, WorkspaceWorkerQueryRepositoryImpl.class})
class PostingApplicationConcurrencyTests {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", postgres::getJdbcUrl);
        properties.add("spring.datasource.username", postgres::getUsername);
        properties.add("spring.datasource.password", postgres::getPassword);
        properties.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired UpdateUserPostingApplicationStatus cancel;
    @Autowired ManagerUpdatePostingApplicationStatus accept;
    @Autowired ManagerUpdatePostingStatus close;

    enum Action { CANCEL, ACCEPT, CLOSE }
    record Fixture(Long userId, Long managerId, Long workspaceId, Long postingId, Long applicationId) {}

    private TransactionTemplate tx() { return new TransactionTemplate(transactionManager); }

    private Fixture fixture(PostingApplicationStatus initialStatus) {
        return tx().execute(status -> {
            User applicant = newUser("지원자");
            ManagerUser manager = ManagerUser.create(newUser("관리자"), ManagerUserStatus.ACTIVATED);
            em.persist(manager);
            BusinessType business = BusinessType.create("업종" + System.nanoTime(), "설명");
            em.persist(business);
            Workspace workspace = Workspace.create(manager, "1234567890", "동시성 검증 업장", business, null,
                "01000000000", "설명", WorkspaceStatus.ACTIVATED, "서울", "서울", "강남구", "역삼동",
                new BigDecimal("37.500000"), new BigDecimal("127.000000"));
            em.persist(workspace);
            Posting posting = Posting.create(new CreatePostingCommand(workspace.getId(), "공고", "설명", 12000,
                2, PaymentType.HOURLY, List.of(new PostingScheduleCommand(List.of(DayOfWeek.MONDAY),
                    LocalTime.of(9, 0), LocalTime.of(18, 0), "홀서빙"))), workspace);
            em.persist(posting);
            PostingApplication application = PostingApplication.create(posting.getSchedules().getFirst(), applicant, "지원");
            application.updateStatus(initialStatus);
            em.persist(application);
            return new Fixture(applicant.getId(), manager.getId(), workspace.getId(), posting.getId(), application.getId());
        });
    }

    private User newUser(String name) {
        User user = User.create("01000000000", "encoded", name, "race" + System.nanoTime(),
            UserGender.GENDER_MALE, "19990101", null);
        em.persist(user);
        return user;
    }

    private void execute(Action action, Fixture fixture) {
        // 지원서를 미리 로딩하지 않고 실제 Actor 인증에서 로딩하는 엔티티만 준비한다.
        switch (action) {
            case CANCEL -> cancel.execute(AppActor.from(em.find(User.class, fixture.userId()), List.of()),
                fixture.applicationId(), new UpdateUserPostingApplicationStatusRequestDto(PostingApplicationStatus.CANCELLED));
            case ACCEPT -> accept.execute(fixture.applicationId(),
                new UpdatePostingApplicationStatusRequestDto(PostingApplicationStatus.ACCEPTED),
                ManagerActor.from(em.find(ManagerUser.class, fixture.managerId()), List.of()));
            case CLOSE -> close.execute(fixture.postingId(), new UpdatePostingStatusRequestDto(PostingStatus.CLOSED),
                ManagerActor.from(em.find(ManagerUser.class, fixture.managerId()), List.of()));
        }
    }

    private void await(CountDownLatch latch) {
        try {
            assertThat(latch.await(20, TimeUnit.SECONDS)).as("트랜잭션 동기화").isTrue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    private void awaitDatabaseLock(int pid) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(4);
        while (System.nanoTime() < deadline) {
            Boolean blocked = jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM pg_stat_activity WHERE pid = ? AND wait_event_type = 'Lock')",
                Boolean.class, pid);
            if (Boolean.TRUE.equals(blocked)) return;
            Thread.sleep(25);
        }
        fail("두 번째 트랜잭션이 PostgreSQL 행 잠금에 대기하지 않았습니다. pid=" + pid);
    }

    @ParameterizedTest
    @CsvSource({
        "ACCEPT, CANCEL, SUBMITTED", "CANCEL, ACCEPT, SUBMITTED",
        "CLOSE, CANCEL, SUBMITTED", "CANCEL, CLOSE, SUBMITTED",
        "ACCEPT, CANCEL, SHORTLISTED", "CANCEL, ACCEPT, SHORTLISTED",
        "CLOSE, CANCEL, SHORTLISTED", "CANCEL, CLOSE, SHORTLISTED"
    })
    void competingTransitionsReadTheCommittedStateAfterWaiting(
        Action first, Action second, PostingApplicationStatus initialStatus
    ) throws Exception {
        Fixture fixture = fixture(initialStatus);
        CountDownLatch firstUpdated = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        AtomicInteger secondPid = new AtomicInteger();

        try (var executor = Executors.newFixedThreadPool(2)) {
            var firstResult = executor.submit(() -> tx().executeWithoutResult(status -> {
                execute(first, fixture);
                firstUpdated.countDown();
                await(releaseFirst);
            }));
            try {
                await(firstUpdated);
                var secondResult = executor.submit(() -> {
                    try {
                        tx().executeWithoutResult(status -> {
                            secondPid.set(((Number) em.createNativeQuery("SELECT pg_backend_pid()").getSingleResult()).intValue());
                            secondStarted.countDown();
                            execute(second, fixture);
                        });
                        return null;
                    } catch (RuntimeException e) {
                        return e;
                    }
                });
                await(secondStarted);
                awaitDatabaseLock(secondPid.get());
                releaseFirst.countDown();
                firstResult.get(10, TimeUnit.SECONDS);
                RuntimeException failure = secondResult.get(10, TimeUnit.SECONDS);
                if (second == Action.CLOSE) assertThat(failure).isNull();
                else assertThat(failure).isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POSTING_APPLICATION_STATUS_NOT_UPDATABLE);
            } finally {
                releaseFirst.countDown();
            }
        }

        tx().executeWithoutResult(status -> {
            PostingApplicationStatus expected = switch (first) {
                case CANCEL -> PostingApplicationStatus.CANCELLED;
                case ACCEPT -> PostingApplicationStatus.ACCEPTED;
                case CLOSE -> PostingApplicationStatus.REJECTED;
            };
            assertThat(em.find(PostingApplication.class, fixture.applicationId()).getStatus()).isEqualTo(expected);
            Long workers = em.createQuery("select count(w) from WorkspaceWorker w where w.workspace.id = :workspace "
                + "and w.user.id = :user and w.status = 'ACTIVATED'", Long.class)
                .setParameter("workspace", fixture.workspaceId()).setParameter("user", fixture.userId()).getSingleResult();
            assertThat(workers).isEqualTo(first == Action.ACCEPT ? 1 : 0);
            assertThat(em.find(Posting.class, fixture.postingId()).getStatus())
                .isEqualTo(first == Action.CLOSE || second == Action.CLOSE ? PostingStatus.CLOSED : PostingStatus.OPEN);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"postings", "posting_applications"})
    void actualPostingAndFlushLockTimeoutsMapToExisting429Error(String lockedTable) throws Exception {
        Fixture fixture = fixture(PostingApplicationStatus.SUBMITTED);
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        long id = lockedTable.equals("postings") ? fixture.postingId() : fixture.applicationId();
        try (var executor = Executors.newSingleThreadExecutor()) {
            var holder = executor.submit(() -> tx().executeWithoutResult(status -> {
                em.createNativeQuery("SELECT id FROM " + lockedTable + " WHERE id = :id FOR UPDATE")
                    .setParameter("id", id).getSingleResult();
                locked.countDown();
                await(release);
            }));
            try {
                await(locked);
                assertThatThrownBy(() -> tx().executeWithoutResult(status -> execute(Action.CANCEL, fixture)))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_MANY_REQUESTS);
                assertThat(ErrorCode.TOO_MANY_REQUESTS.getCode()).isEqualTo("E001");
                assertThat(ErrorCode.TOO_MANY_REQUESTS.getStatus()).isEqualTo(429);
            } finally {
                release.countDown();
            }
            holder.get(10, TimeUnit.SECONDS);
        }
        tx().executeWithoutResult(status -> assertThat(em.find(PostingApplication.class, fixture.applicationId()).getStatus())
            .isEqualTo(PostingApplicationStatus.SUBMITTED));
    }
}
