package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingApplicationStatusRequestDto;
import com.dreamteam.alter.application.notification.FcmNotificationEvent;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.posting.entity.PostingSchedule;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceWorkerUseCase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.PessimisticLockingFailureException;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ManagerUpdatePostingApplicationStatusTests {

    @Mock PostingApplicationQueryRepository postingApplicationQueryRepository;
    @Mock PostingQueryRepository postingQueryRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock CreateWorkspaceWorkerUseCase createWorkspaceWorker;
    @Mock EntityManager entityManager;
    @InjectMocks ManagerUpdatePostingApplicationStatus useCase;

    private ManagerActor actor(ManagerUser manager) {
        ManagerActor actor = mock(ManagerActor.class);
        given(actor.getManagerUser()).willReturn(manager);
        return actor;
    }

    private PostingApplication application(Posting posting) {
        Workspace workspace = mock(Workspace.class);
        lenient().when(posting.getWorkspace()).thenReturn(workspace);
        lenient().when(workspace.getBusinessName()).thenReturn("알터 카페");
        PostingSchedule schedule = mock(PostingSchedule.class);
        given(schedule.getPosting()).willReturn(posting);
        User user = mock(User.class);
        lenient().when(user.getId()).thenReturn(11L);
        return spy(PostingApplication.create(schedule, user, "지원"));
    }

    private void foundApplication(ManagerUser manager, Posting posting, PostingApplication application) {
        given(postingApplicationQueryRepository.findPostingIdByManagerAndApplicationId(manager, 7L))
            .willReturn(Optional.of(1L));
        given(postingQueryRepository.findByManagerAndIdWithPessimisticLock(1L, manager))
            .willReturn(Optional.of(posting));
        given(postingApplicationQueryRepository.getByManagerAndId(manager, 7L))
            .willReturn(Optional.of(application));
    }

    @ParameterizedTest
    @EnumSource(value = PostingApplicationStatus.class, names = {"SUBMITTED", "SHORTLISTED"})
    void locksPostingBeforeReadingAndUpdatingPendingApplication(PostingApplicationStatus initialStatus) {
        ManagerUser manager = mock(ManagerUser.class);
        Posting posting = mock(Posting.class);
        PostingApplication application = application(posting);
        application.updateStatus(initialStatus);
        foundApplication(manager, posting, application);

        useCase.execute(7L, new UpdatePostingApplicationStatusRequestDto(PostingApplicationStatus.REJECTED), actor(manager));

        InOrder order = inOrder(postingApplicationQueryRepository, postingQueryRepository, application, entityManager,
            eventPublisher);
        order.verify(postingApplicationQueryRepository).findPostingIdByManagerAndApplicationId(manager, 7L);
        order.verify(postingQueryRepository).findByManagerAndIdWithPessimisticLock(1L, manager);
        order.verify(postingApplicationQueryRepository).getByManagerAndId(manager, 7L);
        order.verify(application).getStatus();
        order.verify(application).updateStatus(PostingApplicationStatus.REJECTED);
        order.verify(entityManager).flush();
        ArgumentCaptor<FcmNotificationEvent> captor = ArgumentCaptor.forClass(FcmNotificationEvent.class);
        order.verify(eventPublisher).publishEvent(captor.capture());
        assertThat(application.getStatus()).isEqualTo(PostingApplicationStatus.REJECTED);
        assertThat(captor.getValue().request().getTargetUserId()).isEqualTo(11L);
        assertThat(captor.getValue().request().getBody()).isEqualTo("알터 카페 지원 결과: 불합격");
        verifyNoInteractions(createWorkspaceWorker);
    }

    @Test
    void acceptanceCreatesWorkerAndFlushesBeforeNotification() {
        ManagerUser manager = mock(ManagerUser.class);
        Posting posting = mock(Posting.class);
        PostingApplication application = application(posting);
        foundApplication(manager, posting, application);

        useCase.execute(7L, new UpdatePostingApplicationStatusRequestDto(PostingApplicationStatus.ACCEPTED), actor(manager));

        InOrder order = inOrder(application, createWorkspaceWorker, entityManager, eventPublisher);
        order.verify(application).updateStatus(PostingApplicationStatus.ACCEPTED);
        order.verify(createWorkspaceWorker).execute(posting.getWorkspace(), application.getUser());
        order.verify(entityManager).flush();
        order.verify(eventPublisher).publishEvent(any(FcmNotificationEvent.class));
        assertThat(application.getStatus()).isEqualTo(PostingApplicationStatus.ACCEPTED);
    }

    @Test
    void rereadsApplicationStateAfterWaitingForPostingLock() {
        ManagerUser manager = mock(ManagerUser.class);
        Posting posting = mock(Posting.class);
        PostingApplication application = application(posting);
        given(postingApplicationQueryRepository.findPostingIdByManagerAndApplicationId(manager, 7L))
            .willReturn(Optional.of(1L));
        given(postingQueryRepository.findByManagerAndIdWithPessimisticLock(1L, manager)).willAnswer(invocation -> {
            application.updateStatus(PostingApplicationStatus.REJECTED);
            return Optional.of(posting);
        });
        given(postingApplicationQueryRepository.getByManagerAndId(manager, 7L)).willReturn(Optional.of(application));

        assertThatThrownBy(() -> useCase.execute(7L,
            new UpdatePostingApplicationStatusRequestDto(PostingApplicationStatus.ACCEPTED), actor(manager)))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POSTING_APPLICATION_STATUS_NOT_UPDATABLE);

        assertThat(application.getStatus()).isEqualTo(PostingApplicationStatus.REJECTED);
        verifyNoInteractions(createWorkspaceWorker, entityManager, eventPublisher);
    }

    @ParameterizedTest
    @EnumSource(value = PostingApplicationStatus.class, names = {"ACCEPTED", "CANCELLED", "REJECTED", "EXPIRED"})
    void terminalApplicationsAreRejectedAfterPostingLock(PostingApplicationStatus initialStatus) {
        ManagerUser manager = mock(ManagerUser.class);
        Posting posting = mock(Posting.class);
        PostingApplication application = application(posting);
        application.updateStatus(initialStatus);
        foundApplication(manager, posting, application);

        assertThatThrownBy(() -> useCase.execute(7L,
            new UpdatePostingApplicationStatusRequestDto(PostingApplicationStatus.ACCEPTED), actor(manager)))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POSTING_APPLICATION_STATUS_NOT_UPDATABLE);

        then(postingQueryRepository).should().findByManagerAndIdWithPessimisticLock(1L, manager);
        assertThat(application.getStatus()).isEqualTo(initialStatus);
        verifyNoInteractions(createWorkspaceWorker, entityManager, eventPublisher);
    }

    @Test
    void missingOrUnownedApplicationDoesNotLockPosting() {
        ManagerUser manager = mock(ManagerUser.class);
        given(postingApplicationQueryRepository.findPostingIdByManagerAndApplicationId(manager, 7L))
            .willReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L,
            new UpdatePostingApplicationStatusRequestDto(PostingApplicationStatus.ACCEPTED), actor(manager)))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POSTING_APPLICATION_NOT_FOUND);

        verifyNoInteractions(postingQueryRepository, createWorkspaceWorker, entityManager, eventPublisher);
        then(postingApplicationQueryRepository).should().findPostingIdByManagerAndApplicationId(manager, 7L);
        then(postingApplicationQueryRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void missingPostingAfterScalarLookupDoesNotReadApplication() {
        ManagerUser manager = mock(ManagerUser.class);
        given(postingApplicationQueryRepository.findPostingIdByManagerAndApplicationId(manager, 7L))
            .willReturn(Optional.of(1L));
        given(postingQueryRepository.findByManagerAndIdWithPessimisticLock(1L, manager))
            .willReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L,
            new UpdatePostingApplicationStatusRequestDto(PostingApplicationStatus.ACCEPTED), actor(manager)))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POSTING_NOT_FOUND);

        then(postingApplicationQueryRepository).should().findPostingIdByManagerAndApplicationId(manager, 7L);
        then(postingApplicationQueryRepository).shouldHaveNoMoreInteractions();
        verifyNoInteractions(createWorkspaceWorker, entityManager, eventPublisher);
    }

    @Test
    void missingApplicationAfterPostingLockDoesNotMutateOrNotify() {
        ManagerUser manager = mock(ManagerUser.class);
        Posting posting = mock(Posting.class);
        given(postingApplicationQueryRepository.findPostingIdByManagerAndApplicationId(manager, 7L))
            .willReturn(Optional.of(1L));
        given(postingQueryRepository.findByManagerAndIdWithPessimisticLock(1L, manager))
            .willReturn(Optional.of(posting));
        given(postingApplicationQueryRepository.getByManagerAndId(manager, 7L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L,
            new UpdatePostingApplicationStatusRequestDto(PostingApplicationStatus.ACCEPTED), actor(manager)))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POSTING_APPLICATION_NOT_FOUND);

        verifyNoInteractions(createWorkspaceWorker, entityManager, eventPublisher);
    }

    @ParameterizedTest
    @MethodSource("lockFailures")
    void postingLockFailuresMapToTooManyRequests(RuntimeException failure) {
        ManagerUser manager = mock(ManagerUser.class);
        given(postingApplicationQueryRepository.findPostingIdByManagerAndApplicationId(manager, 7L))
            .willReturn(Optional.of(1L));
        given(postingQueryRepository.findByManagerAndIdWithPessimisticLock(1L, manager)).willThrow(failure);

        assertThatThrownBy(() -> useCase.execute(7L,
            new UpdatePostingApplicationStatusRequestDto(PostingApplicationStatus.ACCEPTED), actor(manager)))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_MANY_REQUESTS);

        then(postingApplicationQueryRepository).should().findPostingIdByManagerAndApplicationId(manager, 7L);
        then(postingApplicationQueryRepository).shouldHaveNoMoreInteractions();
        verifyNoInteractions(createWorkspaceWorker, entityManager, eventPublisher);
    }

    @ParameterizedTest
    @MethodSource("lockFailures")
    void flushLockFailuresMapToTooManyRequestsWithoutNotification(RuntimeException failure) {
        ManagerUser manager = mock(ManagerUser.class);
        Posting posting = mock(Posting.class);
        PostingApplication application = application(posting);
        foundApplication(manager, posting, application);
        doThrow(failure).when(entityManager).flush();

        assertThatThrownBy(() -> useCase.execute(7L,
            new UpdatePostingApplicationStatusRequestDto(PostingApplicationStatus.ACCEPTED), actor(manager)))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_MANY_REQUESTS);

        then(createWorkspaceWorker).should().execute(posting.getWorkspace(), application.getUser());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void invalidRequestedStatusIsRejectedBeforeQueries() {
        assertThatThrownBy(() -> useCase.execute(7L,
            new UpdatePostingApplicationStatusRequestDto(PostingApplicationStatus.CANCELLED), mock(ManagerActor.class)))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ILLEGAL_ARGUMENT);

        verifyNoInteractions(postingApplicationQueryRepository, postingQueryRepository, createWorkspaceWorker,
            entityManager, eventPublisher);
    }

    private static Stream<RuntimeException> lockFailures() {
        return Stream.of(new PessimisticLockingFailureException("timeout"), new LockTimeoutException("timeout"),
            new PessimisticLockException("timeout"));
    }
}
