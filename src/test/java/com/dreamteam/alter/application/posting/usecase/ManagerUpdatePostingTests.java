package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.command.UpdatePostingCommand;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManagerUpdatePosting 테스트")
class ManagerUpdatePostingTests {

    @Mock
    private PostingQueryRepository postingQueryRepository;

    @InjectMocks
    private ManagerUpdatePosting managerUpdatePosting;

    @Test
    @DisplayName("존재하지 않거나 자신의 공고가 아니면 POSTING_NOT_FOUND 예외가 발생한다")
    void execute_공고없음_예외발생() {
        // given
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser managerUser = mock(ManagerUser.class);
        given(actor.getManagerUser()).willReturn(managerUser);
        given(postingQueryRepository.findByManagerAndId(1L, managerUser)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> managerUpdatePosting.execute(1L, command(), actor))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.POSTING_NOT_FOUND));
    }

    @Test
    @DisplayName("삭제된 공고는 내용을 수정할 수 없다")
    void execute_삭제된공고_예외발생() {
        // given
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser managerUser = mock(ManagerUser.class);
        Posting posting = mock(Posting.class);

        given(actor.getManagerUser()).willReturn(managerUser);
        given(postingQueryRepository.findByManagerAndId(1L, managerUser)).willReturn(Optional.of(posting));
        given(posting.getStatus()).willReturn(PostingStatus.DELETED);

        // when & then
        assertThatThrownBy(() -> managerUpdatePosting.execute(1L, command(), actor))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));
        then(posting).should(never()).updateContent(any());
    }

    @Test
    @DisplayName("모집 중인 공고는 내용이 수정된다")
    void execute_정상공고_수정() {
        // given
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser managerUser = mock(ManagerUser.class);
        Posting posting = mock(Posting.class);
        UpdatePostingCommand command = command();

        given(actor.getManagerUser()).willReturn(managerUser);
        given(postingQueryRepository.findByManagerAndId(1L, managerUser)).willReturn(Optional.of(posting));
        given(posting.getStatus()).willReturn(PostingStatus.OPEN);

        // when
        managerUpdatePosting.execute(1L, command, actor);

        // then
        then(posting).should().updateContent(command);
    }

    private UpdatePostingCommand command() {
        return new UpdatePostingCommand(
            "수정된 제목",
            "수정된 설명",
            13000,
            PaymentType.HOURLY,
            List.of(),
            List.of(),
            List.of()
        );
    }
}
