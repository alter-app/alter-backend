package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.outbound.user.persistence.ManagerUserQueryRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.user.persistence.ManagerUserRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.*;
import com.dreamteam.alter.application.file.FileDeleteService;
import com.dreamteam.alter.common.config.QueryDslConfig;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.exception.handler.GlobalExceptionHandler;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.type.UserGender;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.type.WorkspaceRequestStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.*;

@Testcontainers
@DataJpaTest(showSql = false, properties = {"spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.hibernate.ddl-auto=create", "spring.jpa.show-sql=false"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(PersistenceExceptionTranslationAutoConfiguration.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import({QueryDslConfig.class, WorkspaceRequestQueryRepositoryImpl.class, WorkspaceRepositoryImpl.class,
    ManagerUserQueryRepositoryImpl.class, ManagerUserRepositoryImpl.class, WorkspaceImageRepositoryImpl.class,
    WorkspaceRequestImageQueryRepositoryImpl.class, CancelWorkspaceRequest.class, UpdateWorkspaceRequestStatus.class})
class WorkspaceRequestConcurrencyTests {
    @Container static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2");
    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", postgres::getJdbcUrl);
        properties.add("spring.datasource.username", postgres::getUsername);
        properties.add("spring.datasource.password", postgres::getPassword);
        properties.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired CancelWorkspaceRequest cancel;
    @Autowired UpdateWorkspaceRequestStatus update;
    @MockitoSpyBean WorkspaceRequestQueryRepositoryImpl requests;
    @MockitoBean FileQueryRepository files;
    @MockitoBean FileDeleteService delete;

    enum Winner { CANCEL, APPROVE }
    record Fixture(Long requestId, Long userId) {}

    @AfterEach
    void resetSpy() { reset(requests); }

    @Test
    void ownPendingCancellationMatchesDatabaseListAndDetail() {
        Fixture fixture = fixture();
        action(fixture, Winner.CANCEL);
        assertStored(fixture, WorkspaceRequestStatus.CANCELLED, 0);
        verifyNoInteractions(delete);
    }

    @Test
    void missingRequestKeepsNotFoundContract() {
        Fixture fixture = fixture();
        assertThatThrownBy(() -> tx().executeWithoutResult(status ->
            cancel.execute(em.find(User.class, fixture.userId()), Long.MAX_VALUE)))
            .isInstanceOf(CustomException.class).hasMessage("등록 신청한 업장을 찾을 수 없습니다.")
            .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
        assertStored(fixture, WorkspaceRequestStatus.PENDING, 0);
        verifyNoInteractions(delete);
    }

    @Test
    void otherUserCannotCancelTheRequest() {
        Fixture fixture = fixture();
        Long otherId = tx().execute(status -> {
            User other = User.create("01000000000", "encoded", "타인", "other" + System.nanoTime(),
                UserGender.GENDER_MALE, "19990101", null);
            em.persist(other);
            return other.getId();
        });
        assertThatThrownBy(() -> tx().executeWithoutResult(status ->
            cancel.execute(em.find(User.class, otherId), fixture.requestId())))
            .isInstanceOf(CustomException.class).hasMessage("등록 신청한 업장을 찾을 수 없습니다.")
            .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
        assertStored(fixture, WorkspaceRequestStatus.PENDING, 0);
        verifyNoInteractions(delete);
    }

    @ParameterizedTest
    @EnumSource(value = WorkspaceRequestStatus.class, names = {"ACTIVATED", "REVOKED", "CANCELLED"})
    void cancellationOfNonPendingStateKeepsConflictContract(WorkspaceRequestStatus state) {
        Fixture fixture = fixture();
        if (state == WorkspaceRequestStatus.CANCELLED) action(fixture, Winner.CANCEL);
        else update.execute(fixture.requestId(), state);
        assertThatThrownBy(() -> action(fixture, Winner.CANCEL)).isInstanceOf(CustomException.class)
            .hasMessage("취소할 수 없는 상태입니다.").extracting("errorCode").isEqualTo(ErrorCode.CONFLICT);
        assertStored(fixture, state, state == WorkspaceRequestStatus.ACTIVATED ? 1 : 0);
    }

    @Test
    void rejectedRequestCanStillBeReconsideredForApproval() {
        Fixture fixture = fixture();
        update.execute(fixture.requestId(), WorkspaceRequestStatus.REVOKED);
        action(fixture, Winner.APPROVE);
        assertStored(fixture, WorkspaceRequestStatus.ACTIVATED, 1);
    }

    @Test
    void fileDeletionFailureRollsBackCancellation() {
        Fixture fixture = fixture();
        File identity = mock(File.class);
        when(files.findByTargetTypeAndTargetId(FileTargetType.WORKSPACE_OWN_IDENTITY, fixture.requestId().toString()))
            .thenReturn(Optional.of(identity));
        doAnswer(invocation -> {
            em.flush();
            assertThat(em.find(WorkspaceRequest.class, fixture.requestId()).getStatus()).isEqualTo(WorkspaceRequestStatus.CANCELLED);
            throw new IllegalStateException("mock file deletion failure");
        }).when(delete).delete(identity);
        assertThatThrownBy(() -> action(fixture, Winner.CANCEL)).isInstanceOf(IllegalStateException.class);
        assertStored(fixture, WorkspaceRequestStatus.PENDING, 0);
        verify(delete).delete(identity);
    }

    @ParameterizedTest
    @EnumSource(Winner.class)
    void requestLockTimeoutUsesExisting429HandlerAndKeepsStorage(Winner operation) throws Exception {
        Fixture fixture = fixture();
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try (var executor = Executors.newSingleThreadExecutor()) {
            var holder = executor.submit(() -> tx().executeWithoutResult(status -> {
                requests.findByIdForUpdate(fixture.requestId());
                locked.countDown();
                try { assertThat(release.await(15, TimeUnit.SECONDS)).isTrue(); }
                catch (InterruptedException exception) { throw new RuntimeException(exception); }
            }));
            assertThat(locked.await(5, TimeUnit.SECONDS)).isTrue();
            try {
                PessimisticLockingFailureException failure = catchThrowableOfType(
                    () -> action(fixture, operation), PessimisticLockingFailureException.class);
                assertThat(failure).isNotNull();
                var response = new GlobalExceptionHandler().handlePessimisticLockingFailureException(failure);
                assertThat(response.getStatusCode().value()).isEqualTo(429);
                assertThat(response.getBody().code()).isEqualTo("E001");
            } finally { release.countDown(); }
            holder.get(5, TimeUnit.SECONDS);
        }
        assertStored(fixture, WorkspaceRequestStatus.PENDING, 0);
        verifyNoInteractions(delete);
    }

    @Test
    void completedCancellationCannotBeApprovedLater() {
        Fixture fixture = fixture();
        action(fixture, Winner.CANCEL);
        ErrorCode approval = attempt(() -> action(fixture, Winner.APPROVE));
        assertStored(fixture, WorkspaceRequestStatus.CANCELLED, 0);
        assertThat(approval).isEqualTo(ErrorCode.CONFLICT);
    }

    @ParameterizedTest
    @EnumSource(Winner.class)
    void approvalAndCancellationHaveOneConsistentWinner(Winner winner) throws Exception {
        Fixture fixture = fixture();
        CountDownLatch firstReady = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        CountDownLatch oldSecondLoaded = new CountDownLatch(1);
        CountDownLatch firstDone = new CountDownLatch(1);
        doAnswer(invocation -> {
            if (Thread.currentThread().getName().equals("first")) {
                firstReady.countDown();
                assertThat(secondStarted.await(5, TimeUnit.SECONDS)).isTrue();
                oldSecondLoaded.await(1, TimeUnit.SECONDS);
            }
            return Optional.empty();
        }).when(files).findByTargetTypeAndTargetId(FileTargetType.WORKSPACE_OWN_IDENTITY, fixture.requestId().toString());
        doAnswer(invocation -> {
            Object result = invocation.callRealMethod();
            if (Thread.currentThread().getName().equals("second")) {
                oldSecondLoaded.countDown();
                assertThat(firstDone.await(5, TimeUnit.SECONDS)).isTrue();
            }
            return result;
        }).when(requests).findByIdWithUser(fixture.requestId());

        ErrorCode secondResult;
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> {
                Thread.currentThread().setName("first");
                try { action(fixture, winner); }
                finally { firstDone.countDown(); }
            });
            assertThat(firstReady.await(5, TimeUnit.SECONDS)).isTrue();
            var second = executor.submit(() -> {
                Thread.currentThread().setName("second");
                secondStarted.countDown();
                return attempt(() -> action(fixture, winner == Winner.CANCEL ? Winner.APPROVE : Winner.CANCEL));
            });
            first.get(15, TimeUnit.SECONDS);
            secondResult = second.get(15, TimeUnit.SECONDS);
        }
        assertStored(fixture, winner == Winner.CANCEL ? WorkspaceRequestStatus.CANCELLED : WorkspaceRequestStatus.ACTIVATED,
            winner == Winner.CANCEL ? 0 : 1);
        assertThat(secondResult).isEqualTo(ErrorCode.CONFLICT);
        verifyNoInteractions(delete);
    }

    private void action(Fixture fixture, Winner action) {
        tx().executeWithoutResult(status -> {
            if (action == Winner.CANCEL) cancel.execute(em.find(User.class, fixture.userId()), fixture.requestId());
            else update.execute(fixture.requestId(), WorkspaceRequestStatus.ACTIVATED);
        });
    }

    private ErrorCode attempt(Runnable action) {
        try { action.run(); return null; }
        catch (CustomException exception) { return exception.getErrorCode(); }
    }

    private void assertStored(Fixture fixture, WorkspaceRequestStatus expected, long workspaces) {
        tx().executeWithoutResult(status -> {
            assertThat(em.find(WorkspaceRequest.class, fixture.requestId()).getStatus()).isEqualTo(expected);
            assertThat(em.createQuery("select count(w) from Workspace w where w.managerUser.user.id = :user", Long.class)
                .setParameter("user", fixture.userId()).getSingleResult()).isEqualTo(workspaces);
            assertThat(requests.getWorkspaceRequest(fixture.userId(), fixture.requestId()).getStatus()).isEqualTo(expected);
            assertThat(requests.getWorkspaceRequestList(fixture.userId())).extracting("status").containsExactly(expected);
        });
    }

    private Fixture fixture() {
        return tx().execute(status -> {
            String unique = String.valueOf(System.nanoTime());
            User user = User.create("010" + unique.substring(0, 8), "encoded", "신청자", "request" + unique,
                UserGender.GENDER_MALE, "19990101", null);
            em.persist(user);
            BusinessType business = BusinessType.create("업종" + unique, null);
            em.persist(business);
            WorkspaceRequest request = WorkspaceRequest.create(user, "123-45-12345", "검증 업장", business, null,
                "01012345678", "서울 구로구 고척동", "서울", "구로구", "고척동", BigDecimal.ONE, BigDecimal.ONE);
            em.persist(request);
            return new Fixture(request.getId(), user.getId());
        });
    }

    private TransactionTemplate tx() { return new TransactionTemplate(transactionManager); }
}
