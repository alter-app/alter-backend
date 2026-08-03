package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.command.CreatePostingCommand;
import com.dreamteam.alter.domain.posting.command.PostingScheduleCommand;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.port.outbound.PostingRepository;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
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
@DisplayName("CreatePosting 테스트")
class CreatePostingTests {

    @Mock
    private PostingRepository postingRepository;

    @Mock
    private WorkspaceQueryRepository workspaceQueryRepository;

    @InjectMocks
    private CreatePosting createPosting;

    @Test
    @DisplayName("자신이 관리하는 업장이 아니면 WORKSPACE_NOT_FOUND 예외가 발생한다")
    void execute_타매니저업장_예외발생() {
        // given
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser managerUser = mock(ManagerUser.class);

        given(actor.getManagerUser()).willReturn(managerUser);
        given(workspaceQueryRepository.findByIdAndManagerUser(1L, managerUser)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> createPosting.execute(command(), actor))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.WORKSPACE_NOT_FOUND));
        then(postingRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("자신이 관리하는 업장이면 공고가 OPEN 상태로 근무일정과 함께 저장된다")
    void execute_소유업장_공고저장() {
        // given
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser managerUser = mock(ManagerUser.class);
        Workspace workspace = mock(Workspace.class);

        given(actor.getManagerUser()).willReturn(managerUser);
        given(workspaceQueryRepository.findByIdAndManagerUser(1L, managerUser)).willReturn(Optional.of(workspace));

        // when
        createPosting.execute(command(), actor);

        // then
        ArgumentCaptor<Posting> captor = ArgumentCaptor.forClass(Posting.class);
        then(postingRepository).should().save(captor.capture());

        Posting saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("홀서빙 구합니다");
        assertThat(saved.getWorkspace()).isSameAs(workspace);
        assertThat(saved.getStatus()).isEqualTo(PostingStatus.OPEN);
        assertThat(saved.getPaymentType()).isEqualTo(PaymentType.HOURLY);
        assertThat(saved.getSchedules()).hasSize(1);
        assertThat(saved.getSchedules().getFirst().getPosition()).isEqualTo("홀서빙");
        assertThat(saved.getSchedules().getFirst().getPositionsAvailable()).isEqualTo(3);
    }

    private CreatePostingCommand command() {
        return new CreatePostingCommand(
            1L,
            "홀서빙 구합니다",
            "주말 근무 가능하신 분",
            12000,
            PaymentType.HOURLY,
            List.of(new PostingScheduleCommand(
                List.of(DayOfWeek.MONDAY),
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                3,
                "홀서빙"
            ))
        );
    }
}
