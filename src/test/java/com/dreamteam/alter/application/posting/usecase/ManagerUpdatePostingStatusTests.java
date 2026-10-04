package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingStatusRequestDto;
import com.dreamteam.alter.application.notification.FcmBatchNotificationEvent;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.notification.NotificationMessageConstants;
import com.dreamteam.alter.domain.posting.command.CreatePostingCommand;
import com.dreamteam.alter.domain.posting.command.PostingScheduleCommand;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.PessimisticLockingFailureException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagerUpdatePostingStatusTests {
    @Mock PostingQueryRepository postingQueryRepository;
    @Mock PostingApplicationQueryRepository postingApplicationQueryRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock EntityManager entityManager;
    @InjectMocks ManagerUpdatePostingStatus useCase;

    private Posting posting() {
        Workspace workspace = mock(Workspace.class);
        lenient().when(workspace.getBusinessName()).thenReturn("알터 카페");
        return Posting.create(new CreatePostingCommand(1L, "공고", "설명", 12000, 1,
            PaymentType.HOURLY, List.of(new PostingScheduleCommand(List.of(DayOfWeek.MONDAY),
                LocalTime.of(9, 0), LocalTime.of(18, 0), "홀서빙"))), workspace);
    }

    private ManagerActor actorWithPosting(Posting posting) {
        ManagerUser manager = mock(ManagerUser.class);
        ManagerActor actor = mock(ManagerActor.class);
        given(actor.getManagerUser()).willReturn(manager);
        given(postingQueryRepository.findByManagerAndIdWithPessimisticLock(1L, manager)).willReturn(Optional.of(posting));
        return actor;
    }

    @ParameterizedTest
    @EnumSource(value = PostingStatus.class, names = {"CLOSED", "CANCELLED", "DELETED"})
    void closingRejectsPendingAndPublishesOneBatchNotification(PostingStatus status) {
        Posting posting = posting();
        ManagerActor actor = actorWithPosting(posting);
        User user = mock(User.class);
        given(user.getId()).willReturn(11L);
        User anotherUser = mock(User.class);
        given(anotherUser.getId()).willReturn(12L);
        PostingApplication submitted = PostingApplication.create(posting.getSchedules().getFirst(), user, "지원");
        PostingApplication shortlisted = PostingApplication.create(posting.getSchedules().getFirst(), anotherUser, "지원");
        shortlisted.updateStatus(PostingApplicationStatus.SHORTLISTED);
        given(postingApplicationQueryRepository.findPendingByPostingIdWithUser(1L))
            .willReturn(List.of(submitted, shortlisted));

        useCase.execute(1L, new UpdatePostingStatusRequestDto(status), actor);

        assertThat(posting.getStatus()).isEqualTo(status);
        assertThat(List.of(submitted, shortlisted)).extracting(PostingApplication::getStatus)
            .containsOnly(PostingApplicationStatus.REJECTED);
        ArgumentCaptor<FcmBatchNotificationEvent> captor = ArgumentCaptor.forClass(FcmBatchNotificationEvent.class);
        then(eventPublisher).should().publishEvent(captor.capture());
        assertThat(captor.getValue().request().getTargetUserIds()).containsExactly(11L, 12L);
        assertThat(captor.getValue().request().getTitle()).isEqualTo(NotificationMessageConstants.PostingApplication.REJECTED_TITLE);
        assertThat(captor.getValue().request().getBody()).isEqualTo("알터 카페 지원 결과: 불합격");
        then(entityManager).should().flush();
        then(postingApplicationQueryRepository).should().findPendingByPostingIdWithUser(1L);
    }

    @Test
    void reopeningDoesNotTouchApplications() {
        Posting posting = posting();
        posting.updateStatus(PostingStatus.CLOSED);
        useCase.execute(1L, new UpdatePostingStatusRequestDto(PostingStatus.OPEN), actorWithPosting(posting));
        assertThat(posting.getStatus()).isEqualTo(PostingStatus.OPEN);
        verifyNoInteractions(postingApplicationQueryRepository, eventPublisher);
    }

    @Test
    void repeatedCloseDoesNotPublishAgainWhenNoPendingApplicationRemains() {
        Posting posting = posting();
        posting.updateStatus(PostingStatus.CLOSED);
        ManagerActor actor = actorWithPosting(posting);
        given(postingApplicationQueryRepository.findPendingByPostingIdWithUser(1L)).willReturn(List.of());
        useCase.execute(1L, new UpdatePostingStatusRequestDto(PostingStatus.CLOSED), actor);
        assertThat(posting.getStatus()).isEqualTo(PostingStatus.CLOSED);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void missingOrUnownedPostingDoesNotTouchApplications() {
        ManagerUser manager = mock(ManagerUser.class);
        ManagerActor actor = mock(ManagerActor.class);
        given(actor.getManagerUser()).willReturn(manager);
        given(postingQueryRepository.findByManagerAndIdWithPessimisticLock(1L, manager)).willReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(1L, new UpdatePostingStatusRequestDto(PostingStatus.CLOSED), actor))
            .isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.POSTING_NOT_FOUND);
        verifyNoInteractions(postingApplicationQueryRepository, eventPublisher);
    }

    @Test
    void lockFailureMapsToExistingTooManyRequestsError() {
        ManagerUser manager = mock(ManagerUser.class);
        ManagerActor actor = mock(ManagerActor.class);
        given(actor.getManagerUser()).willReturn(manager);
        given(postingQueryRepository.findByManagerAndIdWithPessimisticLock(1L, manager))
            .willThrow(new PessimisticLockingFailureException("timeout"));
        assertThatThrownBy(() -> useCase.execute(1L, new UpdatePostingStatusRequestDto(PostingStatus.CLOSED), actor))
            .isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_MANY_REQUESTS);
        verifyNoInteractions(postingApplicationQueryRepository, eventPublisher);
    }

    @Test
    void applicationQueryLockFailureMapsToTooManyRequests() {
        ManagerActor actor = actorWithPosting(posting());
        given(postingApplicationQueryRepository.findPendingByPostingIdWithUser(1L))
            .willThrow(new PessimisticLockingFailureException("timeout"));
        assertThatThrownBy(() -> useCase.execute(1L, new UpdatePostingStatusRequestDto(PostingStatus.CLOSED), actor))
            .isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_MANY_REQUESTS);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void applicationUpdateFlushLockTimeoutMapsToTooManyRequests() {
        assertFlushFailureMapsToTooManyRequests(new LockTimeoutException("timeout"));
    }

    @Test
    void applicationUpdateFlushPessimisticLockFailureMapsToTooManyRequests() {
        assertFlushFailureMapsToTooManyRequests(new PessimisticLockException("timeout"));
    }

    @Test
    void translatedFlushLockFailureMapsToTooManyRequests() {
        assertFlushFailureMapsToTooManyRequests(new PessimisticLockingFailureException("timeout"));
    }

    private void assertFlushFailureMapsToTooManyRequests(RuntimeException failure) {
        Posting posting = posting();
        ManagerActor actor = actorWithPosting(posting);
        PostingApplication submitted = PostingApplication.create(posting.getSchedules().getFirst(), mock(User.class), "지원");
        given(postingApplicationQueryRepository.findPendingByPostingIdWithUser(1L)).willReturn(List.of(submitted));
        doThrow(failure).when(entityManager).flush();
        assertThatThrownBy(() -> useCase.execute(1L, new UpdatePostingStatusRequestDto(PostingStatus.CLOSED), actor))
            .isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_MANY_REQUESTS);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void sameApplicantReceivesOnlyOneRejectionNotification() {
        Posting posting = posting();
        ManagerActor actor = actorWithPosting(posting);
        User user = mock(User.class);
        given(user.getId()).willReturn(11L);
        given(postingApplicationQueryRepository.findPendingByPostingIdWithUser(1L)).willReturn(List.of(
            PostingApplication.create(posting.getSchedules().getFirst(), user, "지원"),
            PostingApplication.create(posting.getSchedules().getFirst(), user, "다른 일정 지원")));
        useCase.execute(1L, new UpdatePostingStatusRequestDto(PostingStatus.CLOSED), actor);
        ArgumentCaptor<FcmBatchNotificationEvent> captor = ArgumentCaptor.forClass(FcmBatchNotificationEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().request().getTargetUserIds()).containsExactly(11L);
    }
}
