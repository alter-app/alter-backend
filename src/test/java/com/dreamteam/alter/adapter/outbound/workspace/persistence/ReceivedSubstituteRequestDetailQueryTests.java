package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequestTarget;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestTargetStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestType;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@Import(SubstituteRequestQueryRepositoryImpl.class)
class ReceivedSubstituteRequestDetailQueryTests extends WorkspaceShiftPersistenceTestSupport {

    @Autowired
    private SubstituteRequestQueryRepositoryImpl repository;
    @Autowired
    private EntityManager entityManager;

    @ParameterizedTest
    @EnumSource(SubstituteRequestStatus.class)
    void returnsStoredRequestAndOnlyOwnTargetStatusIncludingTerminalStates(SubstituteRequestStatus status) {
        Workspace workspace = saveWorkspace();
        WorkspaceWorker requester = saveWorker(workspace, saveUser());
        WorkspaceWorker recipient = saveWorker(workspace, saveUser());
        WorkspaceWorker other = saveWorker(workspace, saveUser());
        SubstituteRequest request = saveRequest(requester, recipient, SubstituteRequestType.ALL);
        request.getTargets().add(SubstituteRequestTarget.create(request, other.getId()));
        ReflectionTestUtils.setField(request, "status", status);
        request.getTargets().getFirst().reject("본인 거절");
        entityManager.flush();
        entityManager.clear();

        var result = repository.getReceivedRequestDetail(recipient.getUser(), request.getId()).orElseThrow();
        assertThat(result.status()).isEqualTo(status);
        assertThat(result.myTargetStatus()).isEqualTo(SubstituteRequestTargetStatus.REJECTED);
        assertThat(result.requesterId()).isEqualTo(requester.getId());
        assertThat(result.workspaceId()).isEqualTo(workspace.getId());
        assertThat(result.scheduleStartDateTime()).isEqualTo(LocalDateTime.of(2026, 10, 12, 9, 0));
        assertThat(repository.getReceivedRequestDetail(other.getUser(), request.getId()).orElseThrow().myTargetStatus())
            .isEqualTo(SubstituteRequestTargetStatus.PENDING);
    }

    @ParameterizedTest
    @EnumSource(SubstituteRequestTargetStatus.class)
    void preservesEveryOwnTargetStatus(SubstituteRequestTargetStatus targetStatus) {
        Workspace workspace = saveWorkspace();
        WorkspaceWorker requester = saveWorker(workspace, saveUser());
        WorkspaceWorker recipient = saveWorker(workspace, saveUser());
        SubstituteRequest request = saveRequest(requester, recipient, SubstituteRequestType.SPECIFIC);
        ReflectionTestUtils.setField(request.getTargets().getFirst(), "status", targetStatus);
        entityManager.flush();
        entityManager.clear();
        assertThat(repository.getReceivedRequestDetail(recipient.getUser(), request.getId()).orElseThrow().myTargetStatus())
            .isEqualTo(targetStatus);
    }

    @Test
    void allRequestDoesNotGrantAccessToNontargetOrRequester() {
        Workspace workspace = saveWorkspace();
        WorkspaceWorker requester = saveWorker(workspace, saveUser());
        WorkspaceWorker recipient = saveWorker(workspace, saveUser());
        WorkspaceWorker nontarget = saveWorker(workspace, saveUser());
        SubstituteRequest request = saveRequest(requester, recipient, SubstituteRequestType.ALL);
        request.getTargets().add(SubstituteRequestTarget.create(request, requester.getId()));
        entityManager.flush();
        assertThat(repository.getReceivedRequestDetail(nontarget.getUser(), request.getId())).isEmpty();
        assertThat(repository.getReceivedRequestDetail(saveUser(), request.getId())).isEmpty();
        assertThat(repository.getReceivedRequestDetail(requester.getUser(), request.getId())).isEmpty();
        assertThat(repository.getReceivedRequestDetail(recipient.getUser(), Long.MAX_VALUE)).isEmpty();
        assertThat(repository.getReceivedRequestDetail(recipient.getUser(), -1L)).isEmpty();
        assertThat(repository.getSentRequestDetail(requester.getUser(), request.getId())).isPresent();
        assertThat(repository.getSentRequestDetail(recipient.getUser(), request.getId())).isEmpty();
    }

    @Test
    void rehiredWorkerCannotReadTargetAddressedToPreviousWorkerRow() {
        Workspace workspace = saveWorkspace();
        WorkspaceWorker requester = saveWorker(workspace, saveUser());
        User recipientUser = saveUser();
        WorkspaceWorker oldWorker = saveWorker(workspace, recipientUser);
        SubstituteRequest request = saveRequest(requester, oldWorker, SubstituteRequestType.SPECIFIC);
        oldWorker.resign();
        entityManager.flush();
        assertThat(repository.getReceivedRequestDetail(recipientUser, request.getId())).isEmpty();
        WorkspaceWorker newWorker = saveWorker(workspace, recipientUser);
        entityManager.flush();
        assertThat(repository.getReceivedRequestDetail(recipientUser, request.getId())).isEmpty();
        SubstituteRequest currentRequest = saveRequest(requester, newWorker, SubstituteRequestType.SPECIFIC);
        assertThat(repository.getReceivedRequestDetail(recipientUser, currentRequest.getId())).isPresent();
    }

    @Test
    void targetWorkerMustBelongToRequestsWorkspace() {
        Workspace workspace = saveWorkspace();
        WorkspaceWorker requester = saveWorker(workspace, saveUser());
        WorkspaceWorker recipient = saveWorker(saveWorkspace(), saveUser());
        SubstituteRequest request = saveRequest(requester, recipient, SubstituteRequestType.ALL);
        assertThat(repository.getReceivedRequestDetail(recipient.getUser(), request.getId())).isEmpty();
    }

    private SubstituteRequest saveRequest(WorkspaceWorker requester, WorkspaceWorker recipient, SubstituteRequestType type) {
        LocalDateTime start = LocalDateTime.of(2026, 10, 12, 9, 0);
        SubstituteRequest request = SubstituteRequest.create(
            saveConfirmedShift(requester, start, start.plusHours(3)), requester.getId(), type, "대타 요청");
        request.getTargets().add(SubstituteRequestTarget.create(request, recipient.getId()));
        entityManager.persist(request);
        return request;
    }
}
