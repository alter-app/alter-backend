package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.command.GetReceivedSubstituteRequestDetailCommand;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetReceivedSubstituteRequestDetailTests {
    @Test
    void unauthorizedAndAbsentRequestsUseSentDetailError() {
        User user = mock(User.class);
        SubstituteRequestQueryRepository repository = mock(SubstituteRequestQueryRepository.class);
        when(repository.getReceivedRequestDetail(user, 3L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> new GetReceivedSubstituteRequestDetail(repository)
            .execute(new GetReceivedSubstituteRequestDetailCommand(user, 3L)))
            .isInstanceOfSatisfying(CustomException.class,
                e -> org.assertj.core.api.Assertions.assertThat(e.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND))
            .hasMessage("존재하지 않는 대타 요청입니다.");
    }
}
