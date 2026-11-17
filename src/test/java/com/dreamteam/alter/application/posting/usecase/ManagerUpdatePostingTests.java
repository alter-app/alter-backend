package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.command.CreatePostingCommand;
import com.dreamteam.alter.domain.posting.command.PostingScheduleCommand;
import com.dreamteam.alter.domain.posting.command.UpdatePostingCommand;
import com.dreamteam.alter.domain.posting.command.UpdatePostingScheduleCommand;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.entity.PostingSchedule;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

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
    @DisplayName("조회한 공고에 수정 명령을 그대로 위임한다")
    void execute_정상공고_수정() {
        // given
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser managerUser = mock(ManagerUser.class);
        Posting posting = mock(Posting.class);
        UpdatePostingCommand command = command();

        given(actor.getManagerUser()).willReturn(managerUser);
        given(postingQueryRepository.findByManagerAndId(1L, managerUser)).willReturn(Optional.of(posting));

        // when
        managerUpdatePosting.execute(1L, command, actor);

        // then
        then(posting).should().updateContent(command);
    }

    @Test
    @DisplayName("추가하는 근무일정 중 하나라도 시작과 종료 시각이 같으면 예외가 발생하고 일정이 추가되지 않는다")
    void execute_추가일정중하나무효_전체거부() {
        // given
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser managerUser = mock(ManagerUser.class);
        Posting posting = postingWithOneSchedule();
        given(actor.getManagerUser()).willReturn(managerUser);
        given(postingQueryRepository.findByManagerAndId(1L, managerUser)).willReturn(Optional.of(posting));

        UpdatePostingCommand command = new UpdatePostingCommand(
            "수정된 제목", "수정된 설명", 13000, 3, PaymentType.HOURLY,
            List.of(
                new PostingScheduleCommand(List.of(DayOfWeek.TUESDAY), LocalTime.of(10, 0), LocalTime.of(19, 0), "홀서빙"),
                new PostingScheduleCommand(List.of(DayOfWeek.WEDNESDAY), LocalTime.of(9, 0), LocalTime.of(9, 0), "홀서빙")
            ),
            List.of(),
            List.of()
        );

        // when & then
        assertThatThrownBy(() -> managerUpdatePosting.execute(1L, command, actor))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT));
        assertThat(posting.getSchedules()).hasSize(2);
    }

    @Test
    @DisplayName("수정하는 근무일정 중 하나라도 시작과 종료 시각이 같으면 예외가 발생한다")
    void execute_수정일정중하나무효_전체거부() {
        // given
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser managerUser = mock(ManagerUser.class);
        Posting posting = postingWithOneSchedule();
        given(actor.getManagerUser()).willReturn(managerUser);
        given(postingQueryRepository.findByManagerAndId(1L, managerUser)).willReturn(Optional.of(posting));

        UpdatePostingCommand command = new UpdatePostingCommand(
            "수정된 제목", "수정된 설명", 13000, 3, PaymentType.HOURLY,
            List.of(),
            List.of(new UpdatePostingScheduleCommand(
                100L, List.of(DayOfWeek.MONDAY), LocalTime.of(9, 0), LocalTime.of(9, 0), "홀서빙")),
            List.of()
        );

        // when & then
        assertThatThrownBy(() -> managerUpdatePosting.execute(1L, command, actor))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT));
        PostingSchedule schedule = posting.getSchedules().iterator().next();
        assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(schedule.getEndTime()).isEqualTo(LocalTime.of(18, 0));
    }

    @Test
    @DisplayName("무효 일정으로 예외가 나면 트랜잭션이 롤백되도록 유스케이스가 Transactional로 감싸져 있다")
    void execute_트랜잭션경계() {
        assertThat(ManagerUpdatePosting.class.isAnnotationPresent(Transactional.class)).isTrue();
    }

    private Posting postingWithOneSchedule() {
        Posting posting = Posting.create(
            new CreatePostingCommand(1L, "제목", "설명", 12000, 3, PaymentType.HOURLY,
                List.of(new PostingScheduleCommand(
                    List.of(DayOfWeek.MONDAY), LocalTime.of(9, 0), LocalTime.of(18, 0), "홀서빙"))),
            mock(Workspace.class)
        );
        ReflectionTestUtils.setField(posting.getSchedules().iterator().next(), "id", 100L);
        return posting;
    }

    private UpdatePostingCommand command() {
        return new UpdatePostingCommand(
            "수정된 제목",
            "수정된 설명",
            13000,
            3,
            PaymentType.HOURLY,
            List.of(),
            List.of(),
            List.of()
        );
    }
}
