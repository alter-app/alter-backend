package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.UpdateWorkScheduleRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManagerUpdateWorkSchedule 테스트")
class ManagerUpdateWorkScheduleTests {

    private static final Long SHIFT_ID = 1L;
    private static final LocalDateTime NEW_START = LocalDateTime.of(2026, 10, 5, 9, 0);
    private static final LocalDateTime NEW_END = LocalDateTime.of(2026, 10, 5, 18, 0);

    @Mock
    private WorkspaceShiftQueryRepository workspaceShiftQueryRepository;
    @Mock private UserQueryRepository userQueryRepository;

    @InjectMocks
    private ManagerUpdateWorkSchedule managerUpdateWorkSchedule;

    @Test
    @DisplayName("배정된 근무자의 다른 근무와 겹치는 시간으로 수정하면 예외가 발생한다")
    void execute_배정_근무자_겹침_예외() {
        ManagerUser managerUser = mock(ManagerUser.class);
        WorkspaceWorker worker = worker();
        WorkspaceShift shift = createShift(managerUser);
        shift.assignWorker(worker);

        when(workspaceShiftQueryRepository.findByIdForUpdate(SHIFT_ID)).thenReturn(Optional.of(shift));
        when(workspaceShiftQueryRepository.hasConflictingSchedule(worker, NEW_START, NEW_END, SHIFT_ID)).thenReturn(true);

        assertThatThrownBy(() -> managerUpdateWorkSchedule.execute(actorOf(managerUser), SHIFT_ID, request()))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
        assertThat(shift.getStartDateTime()).isNotEqualTo(NEW_START);
    }

    @Test
    @DisplayName("겹침이 없으면 시간이 수정된다")
    void execute_겹침_없으면_수정() {
        ManagerUser managerUser = mock(ManagerUser.class);
        WorkspaceWorker worker = worker();
        WorkspaceShift shift = createShift(managerUser);
        shift.assignWorker(worker);

        when(workspaceShiftQueryRepository.findByIdForUpdate(SHIFT_ID)).thenReturn(Optional.of(shift));
        when(workspaceShiftQueryRepository.hasConflictingSchedule(worker, NEW_START, NEW_END, SHIFT_ID)).thenReturn(false);

        managerUpdateWorkSchedule.execute(actorOf(managerUser), SHIFT_ID, request());

        assertThat(shift.getStartDateTime()).isEqualTo(NEW_START);
        assertThat(shift.getEndDateTime()).isEqualTo(NEW_END);
    }

    @Test
    @DisplayName("배정된 근무자가 없으면 겹침 검사 없이 수정된다")
    void execute_미배정이면_검사_생략() {
        ManagerUser managerUser = mock(ManagerUser.class);
        WorkspaceShift shift = createShift(managerUser);

        when(workspaceShiftQueryRepository.findByIdForUpdate(SHIFT_ID)).thenReturn(Optional.of(shift));

        managerUpdateWorkSchedule.execute(actorOf(managerUser), SHIFT_ID, request());

        verify(workspaceShiftQueryRepository, never()).hasConflictingSchedule(any(), any(), any(), any());
        assertThat(shift.getStartDateTime()).isEqualTo(NEW_START);
    }

    @Test
    @DisplayName("시작이 종료보다 늦거나 같은 역전 구간으로 수정하면 예외가 발생하고 시간이 바뀌지 않는다")
    void execute_역전_구간_예외() {
        ManagerUser managerUser = mock(ManagerUser.class);
        WorkspaceShift shift = createShift(managerUser);
        LocalDateTime before = shift.getStartDateTime();

        when(workspaceShiftQueryRepository.findByIdForUpdate(SHIFT_ID)).thenReturn(Optional.of(shift));

        assertThatThrownBy(() -> managerUpdateWorkSchedule.execute(
                actorOf(managerUser), SHIFT_ID, new UpdateWorkScheduleRequestDto(NEW_END, NEW_START, "홀")))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
        assertThat(shift.getStartDateTime()).isEqualTo(before);
    }

    private WorkspaceShift createShift(ManagerUser managerUser) {
        Workspace workspace = mock(Workspace.class);
        when(workspace.getManagerUser()).thenReturn(managerUser);
        return WorkspaceShift.create(
            workspace, NEW_START.minusDays(1), NEW_END.minusDays(1), "홀", WorkspaceShiftStatus.PLANNED);
    }

    private WorkspaceWorker worker() {
        WorkspaceWorker worker = mock(WorkspaceWorker.class);
        User user = mock(User.class);
        when(worker.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(10L);
        return worker;
    }

    private ManagerActor actorOf(ManagerUser managerUser) {
        ManagerActor actor = mock(ManagerActor.class);
        when(actor.getManagerUser()).thenReturn(managerUser);
        return actor;
    }

    private UpdateWorkScheduleRequestDto request() {
        return new UpdateWorkScheduleRequestDto(NEW_START, NEW_END, "홀");
    }
}
