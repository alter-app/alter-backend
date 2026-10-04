package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.outbound.file.persistence.FileQueryRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.BusinessTypeRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.WorkspaceRequestImageRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.WorkspaceRequestRepositoryImpl;
import com.dreamteam.alter.application.file.usecase.AttachFiles;
import com.dreamteam.alter.common.config.QueryDslConfig;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileStatus;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.type.UserGender;
import com.dreamteam.alter.domain.workspace.command.CreateWorkspaceRequestCommand;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@DataJpaTest(showSql = false)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import({QueryDslConfig.class, CreateWorkspaceRequest.class, AttachFiles.class, FileQueryRepositoryImpl.class,
    WorkspaceRequestRepositoryImpl.class, WorkspaceRequestImageRepositoryImpl.class, BusinessTypeRepositoryImpl.class})
class WorkspaceRequestEvidenceTests {
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired CreateWorkspaceRequest create;

    enum DuplicateRole { CERT_IDENTITY, CERT_WARRANT, IDENTITY_WARRANT }
    enum InvalidEvidence { MISSING, OTHER_USER, WRONG_TYPE, USED, DELETED, WRONG_IMAGE }
    record Fixture(User user, Long businessTypeId, String certId, String identityId, String warrantId, List<String> images) {}
    record FileSnapshot(String id, FileStatus status, String targetId) {}

    @ParameterizedTest
    @EnumSource(DuplicateRole.class)
    void sameFileCannotReplaceAnotherRequiredEvidenceRole(DuplicateRole role) {
        Fixture fixture = tx().execute(status -> {
            User user = user();
            BusinessType business = BusinessType.create("업종" + System.nanoTime(), null);
            em.persist(business);
            String cert = file(user, role == DuplicateRole.CERT_IDENTITY ? FileTargetType.WORKSPACE_OWN_IDENTITY :
                role == DuplicateRole.CERT_WARRANT ? FileTargetType.WORKSPACE_WARRANT : FileTargetType.WORKSPACE_CERTIFICATE);
            String identity = role == DuplicateRole.CERT_IDENTITY ? cert : file(user,
                role == DuplicateRole.IDENTITY_WARRANT ? FileTargetType.WORKSPACE_WARRANT : FileTargetType.WORKSPACE_OWN_IDENTITY);
            String warrant = role == DuplicateRole.CERT_IDENTITY ? null : role == DuplicateRole.CERT_WARRANT ? cert : identity;
            return new Fixture(user, business.getId(), cert, identity, warrant, List.of());
        });

        Throwable failure = catchThrowable(() -> create.execute(command(fixture)));

        tx().executeWithoutResult(status -> {
            assertThat(em.createQuery("select count(r) from WorkspaceRequest r where r.user.id = :user", Long.class)
                .setParameter("user", fixture.user().getId()).getSingleResult()).isZero();
            assertThat(em.find(File.class, fixture.certId()).getStatus()).isEqualTo(FileStatus.PENDING);
            assertThat(em.find(File.class, fixture.identityId()).getStatus()).isEqualTo(FileStatus.PENDING);
        });
        assertThat(failure).isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void distinctEvidencePreservesRolesAndCoordinates(boolean withWarrant) {
        Fixture fixture = validFixture(withWarrant);
        create.execute(command(fixture));
        tx().executeWithoutResult(status -> {
            WorkspaceRequest request = em.createQuery("select r from WorkspaceRequest r where r.user.id = :user", WorkspaceRequest.class)
                .setParameter("user", fixture.user().getId()).getSingleResult();
            assertThat(request.getLatitude()).isEqualByComparingTo(BigDecimal.ONE);
            assertThat(request.getLongitude()).isEqualByComparingTo(BigDecimal.ONE);
            assertThat(request.getContact()).isEqualTo("0212345678");
            assertThat(em.find(File.class, fixture.certId()).getTargetType()).isEqualTo(FileTargetType.WORKSPACE_CERTIFICATE);
            assertThat(em.find(File.class, fixture.identityId()).getTargetType()).isEqualTo(FileTargetType.WORKSPACE_OWN_IDENTITY);
            assertThat(em.find(File.class, fixture.certId()).getTargetId()).isEqualTo(request.getId().toString());
            assertThat(em.find(File.class, fixture.identityId()).getStatus()).isEqualTo(FileStatus.ATTACHED);
            if (withWarrant) assertThat(em.find(File.class, fixture.warrantId()).getStatus()).isEqualTo(FileStatus.ATTACHED);
        });
    }

    @Test
    void optionalBlankWarrantKeepsExistingFileNotFoundContract() {
        Fixture original = validFixture(false);
        Fixture fixture = new Fixture(original.user(), original.businessTypeId(), original.certId(), original.identityId(), "", List.of());
        List<String> fileIds = List.of(fixture.certId(), fixture.identityId());
        List<FileSnapshot> before = snapshot(fileIds);
        Throwable failure = catchThrowable(() -> create.execute(command(fixture)));
        assertThat(failure).isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.FILE_NOT_FOUND);
        assertThat(snapshot(fileIds)).isEqualTo(before);
        tx().executeWithoutResult(status -> assertThat(em.createQuery(
            "select count(r) from WorkspaceRequest r where r.user.id = :user", Long.class)
            .setParameter("user", fixture.user().getId()).getSingleResult()).isZero());
    }

    @ParameterizedTest
    @EnumSource(InvalidEvidence.class)
    void invalidEvidenceRollsBackRequestAndEveryFileAttachment(InvalidEvidence invalid) {
        Fixture original = validFixture(false);
        Fixture fixture = tx().execute(status -> {
            String identity = original.identityId();
            List<String> images = List.of();
            switch (invalid) {
                case MISSING -> identity = "missing-file";
                case OTHER_USER -> identity = file(user(), FileTargetType.WORKSPACE_OWN_IDENTITY);
                case WRONG_TYPE -> identity = file(original.user(), FileTargetType.WORKSPACE_CERTIFICATE);
                case USED -> em.find(File.class, identity).attach("previous-target");
                case DELETED -> em.find(File.class, identity).markDeleted();
                case WRONG_IMAGE -> images = List.of(file(original.user(), FileTargetType.WORKSPACE_CERTIFICATE));
            }
            return new Fixture(original.user(), original.businessTypeId(), original.certId(), identity, null, images);
        });
        List<String> fileIds = tx().execute(status -> em.createQuery("select f.id from File f where f.uploadedBy = :user", String.class)
            .setParameter("user", fixture.user().getId()).getResultList());
        List<FileSnapshot> before = snapshot(fileIds);

        Throwable failure = catchThrowable(() -> create.execute(command(fixture)));

        ErrorCode expected = switch (invalid) {
            case MISSING, DELETED -> ErrorCode.FILE_NOT_FOUND;
            case OTHER_USER -> ErrorCode.FORBIDDEN;
            case WRONG_TYPE, WRONG_IMAGE -> ErrorCode.INVALID_FILE;
            case USED -> ErrorCode.FILE_ALREADY_ATTACHED;
        };
        assertThat(failure).isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(expected);
        assertThat(snapshot(fileIds)).isEqualTo(before);
        tx().executeWithoutResult(status -> assertThat(em.createQuery(
            "select count(r) from WorkspaceRequest r where r.user.id = :user", Long.class)
            .setParameter("user", fixture.user().getId()).getSingleResult()).isZero());
    }

    private List<FileSnapshot> snapshot(List<String> fileIds) {
        return tx().execute(status -> em.createQuery("select f from File f where f.id in :ids order by f.id", File.class)
            .setParameter("ids", fileIds).getResultList().stream()
            .map(file -> new FileSnapshot(file.getId(), file.getStatus(), file.getTargetId())).toList());
    }

    private Fixture validFixture(boolean withWarrant) {
        return tx().execute(status -> {
            User user = user();
            BusinessType business = BusinessType.create("업종" + System.nanoTime(), null);
            em.persist(business);
            return new Fixture(user, business.getId(), file(user, FileTargetType.WORKSPACE_CERTIFICATE),
                file(user, FileTargetType.WORKSPACE_OWN_IDENTITY), withWarrant ? file(user, FileTargetType.WORKSPACE_WARRANT) : null,
                List.of());
        });
    }

    private CreateWorkspaceRequestCommand command(Fixture fixture) {
        return new CreateWorkspaceRequestCommand(fixture.user(), "검증 업장", "123-45-12345", "서울 구로구 고척동",
            "서울", "구로구", "고척동", BigDecimal.ONE, BigDecimal.ONE, fixture.businessTypeId(), null,
            "02-1234-5678", fixture.certId(), fixture.identityId(), fixture.warrantId(), fixture.images());
    }

    private User user() {
        String unique = String.valueOf(System.nanoTime());
        User user = User.create("010" + unique.substring(0, 8), "encoded", "검증", "evidence" + unique,
            UserGender.GENDER_MALE, "19990101", null);
        em.persist(user);
        return user;
    }

    private String file(User user, FileTargetType type) {
        File file = File.create(type, "synthetic.pdf", "synthetic/" + UUID.randomUUID(), null,
            "application/pdf", 10L, BucketType.PRIVATE, user.getId());
        em.persist(file);
        return file.getId();
    }

    private TransactionTemplate tx() { return new TransactionTemplate(transactionManager); }
}
