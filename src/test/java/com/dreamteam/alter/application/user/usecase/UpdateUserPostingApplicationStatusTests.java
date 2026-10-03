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
    @EnumSource(value = PostingApplicationStatus.class, names = {"ACCEPTED", "CANCELLED", "SUBMITTED", "SHORTLISTED"})
    void cancelRespectsApplicationStatus(PostingApplicationStatus initialStatus) {
        User user = mock(User.class);
        AppActor actor = mock(AppActor.class);
        given(actor.getUser()).willReturn(user);
        PostingApplication application = PostingApplication.create(mock(PostingSchedule.class), user, "지원");
        application.updateStatus(initialStatus);
        given(repository.getUserPostingApplication(user, 1L)).willReturn(Optional.of(application));
        UpdateUserPostingApplicationStatusRequestDto request = new UpdateUserPostingApplicationStatusRequestDto(PostingApplicationStatus.CANCELLED);
        if (initialStatus == PostingApplicationStatus.ACCEPTED || initialStatus == PostingApplicationStatus.CANCELLED) {
            assertThatThrownBy(() -> useCase.execute(actor, 1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", initialStatus == PostingApplicationStatus.ACCEPTED
                    ? ErrorCode.POSTING_APPLICATION_STATUS_NOT_UPDATABLE : ErrorCode.POSTING_APPLICATION_ALREADY_CANCELLED);
            assertThat(application.getStatus()).isEqualTo(initialStatus);
        } else {
            useCase.execute(actor, 1L, request);
            assertThat(application.getStatus()).isEqualTo(PostingApplicationStatus.CANCELLED);
        }
    }
}
