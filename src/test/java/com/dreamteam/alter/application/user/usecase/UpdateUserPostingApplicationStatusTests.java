package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.UpdateUserPostingApplicationStatusRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.posting.entity.PostingSchedule;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UpdateUserPostingApplicationStatusTests {
    @Mock PostingApplicationQueryRepository repository;
    @InjectMocks UpdateUserPostingApplicationStatus useCase;

    @ParameterizedTest
    @EnumSource(value = PostingApplicationStatus.class, names = {"SUBMITTED", "SHORTLISTED"})
    void cancelsOnlyApplicationsUnderReview(PostingApplicationStatus initialStatus) {
        User user = mock(User.class);
        AppActor actor = mock(AppActor.class);
        given(actor.getUser()).willReturn(user);
        PostingApplication application = PostingApplication.create(mock(PostingSchedule.class), user, "지원");
        application.updateStatus(initialStatus);
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
}
