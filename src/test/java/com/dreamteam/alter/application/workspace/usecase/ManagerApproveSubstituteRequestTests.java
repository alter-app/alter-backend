package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ApproveSubstituteRequestDto;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ManagerApproveSubstituteRequest 테스트")
class ManagerApproveSubstituteRequestTests {

    private static final Long REQUEST_ID = 1L;
    private static final Long SHIFT_ID = 30L;
    private static final Long ACCEPTED_WORKER_ID = 20L;
    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 5, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 10, 5, 18, 0);

    @Mock
    private SubstituteRequestQueryRepository substituteRequestQueryRepository;

    @Mock
    private WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;

    @Mock
    private WorkspaceShiftQueryRepository workspaceShiftQueryRepository;

    @Mock
    private NotificationService notificationService;
    @Mock private UserQueryRepository userQueryRepository;

    @InjectMocks
    private ManagerApproveSubstituteRequest managerApproveSubstituteRequest;

    @Test
    @DisplayName("수락자가 승인 시점에 다른 근무와 겹치면 CONFLICT 예외가 나고 배정되지 않는다")
    void execute_승인_시점_겹침_예외() {
        ManagerUser managerUser = mock(ManagerUser.class);
        WorkspaceShift shift = createShift(managerUser);
        WorkspaceWorker acceptedWorker = mock(WorkspaceWorker.class);
        User acceptedUser = mock(User.class);
        when(acceptedWorker.getUser()).thenReturn(acceptedUser);
        when(acceptedUser.getId()).thenReturn(20L);
        SubstituteRequest request = createAcceptedRequest(shift);

        when(substituteRequestQueryRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request));
        when(workspaceWorkerQueryRepository.findById(ACCEPTED_WORKER_ID)).thenReturn(Optional.of(acceptedWorker));
        when(workspaceShiftQueryRepository.hasConflictingSchedule(acceptedWorker, START, END, SHIFT_ID)).thenReturn(true);

        assertThatThrownBy(() -> managerApproveSubstituteRequest.execute(actorOf(managerUser), REQUEST_ID, new ApproveSubstituteRequestDto("ok")))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.CONFLICT);
        verify(request, never()).approve(any(), anyString());
        assertThat(shift.getAssignedWorkspaceWorker()).isNull();
    }

    @Test
    @DisplayName("겹침이 없으면 승인되고 수락자가 배정된다")
    void execute_겹침_없으면_승인() {
        ManagerUser managerUser = mock(ManagerUser.class);
        WorkspaceShift shift = createShift(managerUser);
        WorkspaceWorker acceptedWorker = mock(WorkspaceWorker.class);
        User acceptedUser = mock(User.class);
        when(acceptedWorker.getUser()).thenReturn(acceptedUser);
        when(acceptedUser.getId()).thenReturn(20L);
        SubstituteRequest request = createAcceptedRequest(shift);

        when(substituteRequestQueryRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request));
        when(workspaceWorkerQueryRepository.findById(ACCEPTED_WORKER_ID)).thenReturn(Optional.of(acceptedWorker));
        when(workspaceShiftQueryRepository.hasConflictingSchedule(acceptedWorker, START, END, SHIFT_ID)).thenReturn(false);

        managerApproveSubstituteRequest.execute(actorOf(managerUser), REQUEST_ID, new ApproveSubstituteRequestDto("ok"));

        verify(request).approve(any(), anyString());
        assertThat(shift.getAssignedWorkspaceWorker()).isEqualTo(acceptedWorker);
    }

    private WorkspaceShift createShift(ManagerUser managerUser) {
        Workspace workspace = mock(Workspace.class);
        when(workspace.getManagerUser()).thenReturn(managerUser);
        WorkspaceShift shift = WorkspaceShift.create(workspace, START, END, "홀", WorkspaceShiftStatus.CONFIRMED);
        ReflectionTestUtils.setField(shift, "id", SHIFT_ID);
        when(workspaceShiftQueryRepository.findByIdForUpdate(SHIFT_ID)).thenReturn(Optional.of(shift));
        return shift;
    }

    private SubstituteRequest createAcceptedRequest(WorkspaceShift shift) {
        SubstituteRequest request = mock(SubstituteRequest.class);
        when(request.getWorkspaceShift()).thenReturn(shift);
        when(request.getStatus()).thenReturn(SubstituteRequestStatus.ACCEPTED);
        when(request.getAcceptedWorkerId()).thenReturn(ACCEPTED_WORKER_ID);
        return request;
    }

    private ManagerActor actorOf(ManagerUser managerUser) {
        ManagerActor actor = mock(ManagerActor.class);
        when(actor.getManagerUser()).thenReturn(managerUser);
        when(managerUser.getId()).thenReturn(7L);
        return actor;
    }
}
