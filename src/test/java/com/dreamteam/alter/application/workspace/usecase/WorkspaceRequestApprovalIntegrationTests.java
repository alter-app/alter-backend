package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.ManagerUserQueryRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.user.persistence.ManagerUserRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.*;
import com.dreamteam.alter.application.file.FileDeleteService;
import com.dreamteam.alter.common.config.QueryDslConfig;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.type.UserGender;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceRequestStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DataJpaTest(showSql = false)
@Import({QueryDslConfig.class, CreateWorkspaceRequest.class, UpdateWorkspaceRequestStatus.class,
    WorkspaceRequestRepositoryImpl.class, WorkspaceRequestQueryRepositoryImpl.class,
    WorkspaceRequestImageRepositoryImpl.class, WorkspaceRequestImageQueryRepositoryImpl.class,
    WorkspaceRepositoryImpl.class, WorkspaceImageRepositoryImpl.class, BusinessTypeRepositoryImpl.class,
    ManagerUserQueryRepositoryImpl.class, ManagerUserRepositoryImpl.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class WorkspaceRequestApprovalIntegrationTests {
    @Autowired private EntityManager em;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private CreateWorkspaceRequest create;
    @Autowired private UpdateWorkspaceRequestStatus update;
    @MockitoBean private AttachFilesUseCase attachFiles;
    @MockitoBean private FileQueryRepository files;
    @MockitoBean private FileDeleteService fileDelete;
    @MockitoBean private WorkspaceRequestQueryRepository requests;

    private TransactionTemplate tx;
    private User user;
    private BusinessType businessType;

    @BeforeEach
    void seed() {
        tx = new TransactionTemplate(transactionManager);
        when(requests.findByIdForUpdate(anyLong())).thenAnswer(invocation ->
            Optional.ofNullable(em.find(WorkspaceRequest.class, invocation.getArgument(0, Long.class))));
        tx.executeWithoutResult(status -> {
            user = User.create("01012345678", "encoded", "연락처 테스트", UUID.randomUUID().toString(),
                UserGender.GENDER_MALE, "19900101", null);
            em.persist(user);
            businessType = BusinessType.create("업종" + UUID.randomUUID(), null);
            em.persist(businessType);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"010-1234-5678", "02-123-4567", "031-1234-5678", "070-1234-5678", "099-1234-5678", "01012345678"})
    void 신규_신청부터_승인까지_숫자로_커밋한다(String contact) {
        create.execute(request(contact).toCommand(user));
        Long id = tx.execute(status -> em.createQuery("select r.id from WorkspaceRequest r where r.user.id = :userId", Long.class)
            .setParameter("userId", user.getId()).getSingleResult());
        tx.executeWithoutResult(status -> assertThat(em.find(WorkspaceRequest.class, id).getContact())
            .isEqualTo(contact.replace("-", "")));

        update.execute(id, WorkspaceRequestStatus.ACTIVATED);

        assertApproved(id, contact.replace("-", ""));
    }

    @Test
    void 기존_13자_하이픈_신청을_11자_업장_컬럼에_승인한다() {
        Long id = legacyRequest("010-1234-5678", "기존 신청");
        update.execute(id, WorkspaceRequestStatus.ACTIVATED);
        assertApproved(id, "01012345678");
    }

    @Test
    void 잘못된_기존_연락처는_400_오류로_거부하고_변경을_롤백한다() {
        Long id = legacyRequest("010--12345678", "잘못된 신청");
        assertThatThrownBy(() -> update.execute(id, WorkspaceRequestStatus.ACTIVATED))
            .isInstanceOfSatisfying(CustomException.class, ex -> {
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
                assertThat(ex.getMessage()).doesNotContain("010--12345678");
            });
        assertRolledBack(id);
    }

    @Test
    void 업장_INSERT_실패시_신청_상태와_신규_매니저까지_롤백한다() {
        Long id = legacyRequest("01012345678", "ALT297_INSERT_FAIL");
        jdbc.execute("ALTER TABLE workspaces ADD CONSTRAINT alt297_insert_failure CHECK (business_name <> 'ALT297_INSERT_FAIL')");
        try {
            assertThatThrownBy(() -> update.execute(id, WorkspaceRequestStatus.ACTIVATED))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasStackTraceContaining("ALT297_INSERT_FAILURE");
            assertRolledBack(id);
        } finally {
            jdbc.execute("ALTER TABLE workspaces DROP CONSTRAINT alt297_insert_failure");
        }
    }

    @Test
    void 업장_INSERT_후_파일처리_실패도_모두_롤백한다() {
        Long id = legacyRequest("01012345678", "후속 처리 실패");
        File identity = mock(File.class);
        when(files.findByTargetTypeAndTargetId(FileTargetType.WORKSPACE_OWN_IDENTITY, id.toString()))
            .thenReturn(Optional.of(identity));
        doAnswer(invocation -> {
            em.flush();
            assertThat(workspaceCount()).isEqualTo(1);
            assertThat(managerCount()).isEqualTo(1);
            throw new IllegalStateException("후속 처리 실패");
        }).when(fileDelete).delete(identity);

        assertThatThrownBy(() -> update.execute(id, WorkspaceRequestStatus.ACTIVATED))
            .isInstanceOf(IllegalStateException.class).hasMessage("후속 처리 실패");
        verify(fileDelete).delete(identity);
        assertRolledBack(id);
    }

    @Test
    void 재승인은_차단하고_두번째_업장은_기존_매니저를_재사용한다() {
        Long first = legacyRequest("01012345678", "첫 업장");
        update.execute(first, WorkspaceRequestStatus.ACTIVATED);
        assertThatThrownBy(() -> update.execute(first, WorkspaceRequestStatus.ACTIVATED))
            .isInstanceOfSatisfying(CustomException.class, ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CONFLICT));
        Long second = legacyRequest("02-1234-5678", "둘째 업장");
        update.execute(second, WorkspaceRequestStatus.ACTIVATED);
        tx.executeWithoutResult(status -> {
            assertThat(managerCount()).isEqualTo(1);
            assertThat(workspaceCount()).isEqualTo(2);
        });
    }

    @Test
    void 반려는_잘못된_기존_연락처에도_가능하다() {
        Long id = legacyRequest("invalid", "반려 신청");
        update.execute(id, WorkspaceRequestStatus.REVOKED);
        tx.executeWithoutResult(status -> {
            assertThat(em.find(WorkspaceRequest.class, id).getStatus()).isEqualTo(WorkspaceRequestStatus.REVOKED);
            assertThat(managerCount()).isZero();
            assertThat(workspaceCount()).isZero();
        });
    }

    private Long legacyRequest(String contact, String name) {
        return tx.execute(status -> {
            WorkspaceRequest request = WorkspaceRequest.create(em.getReference(User.class, user.getId()), "123-45-12345", name,
                em.getReference(BusinessType.class, businessType.getId()), null, contact, "서울 구로구 고척동", "서울", "구로구", "고척동",
                BigDecimal.ONE, BigDecimal.ONE);
            em.persist(request);
            return request.getId();
        });
    }

    private CreateWorkspaceRequestDto request(String contact) {
        return new CreateWorkspaceRequestDto("신규 신청", "123-45-12345", "서울 구로구 고척동", "서울", "구로구", "고척동",
            BigDecimal.ONE, BigDecimal.ONE, businessType.getId(), null, contact, "cert", "identity", null, null);
    }

    private void assertApproved(Long id, String contact) {
        tx.executeWithoutResult(status -> {
            assertThat(em.find(WorkspaceRequest.class, id).getStatus()).isEqualTo(WorkspaceRequestStatus.ACTIVATED);
            Workspace workspace = em.createQuery("select w from Workspace w where w.managerUser.user.id = :userId", Workspace.class)
                .setParameter("userId", user.getId()).getSingleResult();
            assertThat(workspace.getContact()).isEqualTo(contact);
            assertThat(managerCount()).isEqualTo(1);
        });
    }

    private void assertRolledBack(Long id) {
        tx.executeWithoutResult(status -> {
            assertThat(em.find(WorkspaceRequest.class, id).getStatus()).isEqualTo(WorkspaceRequestStatus.PENDING);
            assertThat(managerCount()).isZero();
            assertThat(workspaceCount()).isZero();
        });
    }

    private long managerCount() {
        return em.createQuery("select count(m) from ManagerUser m where m.user.id = :userId", Long.class)
            .setParameter("userId", user.getId()).getSingleResult();
    }

    private long workspaceCount() {
        return em.createQuery("select count(w) from Workspace w where w.managerUser.user.id = :userId", Long.class)
            .setParameter("userId", user.getId()).getSingleResult();
    }
}
