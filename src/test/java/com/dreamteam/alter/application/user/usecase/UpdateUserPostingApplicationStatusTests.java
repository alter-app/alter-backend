package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.UpdateUserPostingApplicationStatusRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.posting.entity.PostingSchedule;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.PessimisticLockingFailureException;
import java.util.Optional;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UpdateUserPostingApplicationStatusTests {
    @Mock PostingApplicationQueryRepository repository;
    @Mock PostingQueryRepository postingQueryRepository;
    @Mock EntityManager entityManager;
    @InjectMocks UpdateUserPostingApplicationStatus useCase;

    @ParameterizedTest
    @EnumSource(value = PostingApplicationStatus.class, names = {"SUBMITTED", "SHORTLISTED"})
    void cancelsOnlyApplicationsUnderReview(PostingApplicationStatus initialStatus) {
        User user = mock(User.class);
        AppActor actor = mock(AppActor.class);
        given(actor.getUser()).willReturn(user);
        PostingApplication application = PostingApplication.create(mock(PostingSchedule.class), user, "지원");
        application.updateStatus(initialStatus);
        given(repository.findPostingIdByUserAndApplicationId(user, 1L)).willReturn(Optional.of(2L));
        given(postingQueryRepository.findByIdWithPessimisticLock(2L)).willReturn(Optional.of(mock(Posting.class)));
        given(repository.getUserPostingApplication(user, 1L)).willReturn(Optional.of(application));
        UpdateUserPostingApplicationStatusRequestDto request = new UpdateUserPostingApplicationStatusRequestDto(PostingApplicationStatus.CANCELLED);
        useCase.execute(actor, 1L, request);
        assertThat(application.getStatus()).isEqualTo(PostingApplicationStatus.CANCELLED);
    }

    @ParameterizedTest
    @EnumSource(value = PostingApplicationStatus.class, names = {"ACCEPTED", "CANCELLED", "REJECTED", "EXPIRED", "DELETED"})
    void rejectsCancellationOfCompletedApplications(PostingApplicationStatus initialStatus) {
        User user = mock(User.class);
        AppActor actor = mock(AppActor.class);
        given(actor.getUser()).willReturn(user);
        PostingApplication application = PostingApplication.create(mock(PostingSchedule.class), user, "지원");
        application.updateStatus(initialStatus);
        given(repository.findPostingIdByUserAndApplicationId(user, 1L)).willReturn(Optional.of(2L));
        given(postingQueryRepository.findByIdWithPessimisticLock(2L)).willReturn(Optional.of(mock(Posting.class)));
        given(repository.getUserPostingApplication(user, 1L)).willReturn(Optional.of(application));
        UpdateUserPostingApplicationStatusRequestDto request = new UpdateUserPostingApplicationStatusRequestDto(PostingApplicationStatus.CANCELLED);

        var exception = assertThatThrownBy(() -> useCase.execute(actor, 1L, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", initialStatus == PostingApplicationStatus.CANCELLED
                ? ErrorCode.POSTING_APPLICATION_ALREADY_CANCELLED : ErrorCode.POSTING_APPLICATION_STATUS_NOT_UPDATABLE);
        if (initialStatus == PostingApplicationStatus.ACCEPTED) {
            exception.hasMessage("합격한 지원서는 취소할 수 없습니다.");
        }
        assertThat(application.getStatus()).isEqualTo(initialStatus);
    }

    @Test
    void locksPostingBeforeReadingApplication() {
        User user = mock(User.class);
        AppActor actor = mock(AppActor.class);
        given(actor.getUser()).willReturn(user);
        PostingApplication application = PostingApplication.create(mock(PostingSchedule.class), user, "지원");
        given(repository.findPostingIdByUserAndApplicationId(user, 1L)).willReturn(Optional.of(2L));
        given(postingQueryRepository.findByIdWithPessimisticLock(2L)).willReturn(Optional.of(mock(Posting.class)));
        given(repository.getUserPostingApplication(user, 1L)).willReturn(Optional.of(application));

        useCase.execute(actor, 1L, new UpdateUserPostingApplicationStatusRequestDto(PostingApplicationStatus.CANCELLED));

        InOrder order = inOrder(repository, postingQueryRepository, entityManager);
        order.verify(repository).findPostingIdByUserAndApplicationId(user, 1L);
        order.verify(postingQueryRepository).findByIdWithPessimisticLock(2L);
        order.verify(repository).getUserPostingApplication(user, 1L);
        order.verify(entityManager).flush();
        assertThat(application.getStatus()).isEqualTo(PostingApplicationStatus.CANCELLED);
    }

    @Test
    void missingApplicationByScalarLookupDoesNotLockPosting() {
        User user = mock(User.class);
        AppActor actor = mock(AppActor.class);
        given(actor.getUser()).willReturn(user);
        given(repository.findPostingIdByUserAndApplicationId(user, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(actor, 1L,
            new UpdateUserPostingApplicationStatusRequestDto(PostingApplicationStatus.CANCELLED)))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POSTING_APPLICATION_NOT_FOUND);

        then(repository).should().findPostingIdByUserAndApplicationId(user, 1L);
        then(repository).shouldHaveNoMoreInteractions();
        verifyNoInteractions(postingQueryRepository, entityManager);
    }

    @ParameterizedTest
    @MethodSource("lockFailures")
    void postingLockFailuresMapToTooManyRequests(RuntimeException failure) {
        User user = mock(User.class);
        AppActor actor = mock(AppActor.class);
        given(actor.getUser()).willReturn(user);
        given(repository.findPostingIdByUserAndApplicationId(user, 1L)).willReturn(Optional.of(2L));
        given(postingQueryRepository.findByIdWithPessimisticLock(2L)).willThrow(failure);

        assertThatThrownBy(() -> useCase.execute(actor, 1L,
            new UpdateUserPostingApplicationStatusRequestDto(PostingApplicationStatus.CANCELLED)))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_MANY_REQUESTS);

        then(repository).should().findPostingIdByUserAndApplicationId(user, 1L);
        then(repository).shouldHaveNoMoreInteractions();
        verifyNoInteractions(entityManager);
    }

    @ParameterizedTest
    @MethodSource("lockFailures")
    void flushLockFailuresMapToTooManyRequests(RuntimeException failure) {
        User user = mock(User.class);
        AppActor actor = mock(AppActor.class);
        given(actor.getUser()).willReturn(user);
        PostingApplication application = PostingApplication.create(mock(PostingSchedule.class), user, "지원");
        given(repository.findPostingIdByUserAndApplicationId(user, 1L)).willReturn(Optional.of(2L));
        given(postingQueryRepository.findByIdWithPessimisticLock(2L)).willReturn(Optional.of(mock(Posting.class)));
        given(repository.getUserPostingApplication(user, 1L)).willReturn(Optional.of(application));
        willThrow(failure).given(entityManager).flush();

        assertThatThrownBy(() -> useCase.execute(actor, 1L,
            new UpdateUserPostingApplicationStatusRequestDto(PostingApplicationStatus.CANCELLED)))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_MANY_REQUESTS);
    }

    private static Stream<RuntimeException> lockFailures() {
        return Stream.of(new PessimisticLockingFailureException("timeout"), new LockTimeoutException("timeout"),
            new PessimisticLockException("timeout"));
    }
}
