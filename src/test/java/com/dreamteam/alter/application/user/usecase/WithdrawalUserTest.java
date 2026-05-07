package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserCertificateRepository;
import com.dreamteam.alter.domain.user.port.outbound.UserRepository;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequestTarget;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawalUser 테스트")
class WithdrawalUserTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCertificateRepository userCertificateRepository;

    @Mock
    private WorkspaceQueryRepository workspaceQueryRepository;

    @Mock
    private WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;

    @Mock
    private WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;

    @Mock
    private SubstituteRequestQueryRepository substituteRequestQueryRepository;

    @Mock
    private PostingApplicationQueryRepository postingApplicationQueryRepository;

    @Mock
    private FileQueryRepository fileQueryRepository;

    @Mock
    private AuthService authService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private WithdrawalUser withdrawalUser;

    @Test
    @DisplayName("활성 업장 보유 시 CONFLICT 예외 발생 및 후속 정리 로직 호출되지 않음")
    void execute_활성업장보유_예외발생() {
        // given
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(workspaceQueryRepository.existsActiveWorkspaceByUserId(1L)).thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> withdrawalUser.execute(user));

        assertEquals(ErrorCode.CONFLICT, exception.getErrorCode());
        verify(user, never()).withdraw();
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userCertificateRepository);
        verifyNoInteractions(workspaceWorkerQueryRepository);
        verifyNoInteractions(workspaceWorkerScheduleQueryRepository);
        verifyNoInteractions(substituteRequestQueryRepository);
        verifyNoInteractions(postingApplicationQueryRepository);
        verifyNoInteractions(fileQueryRepository);
        verifyNoInteractions(authService);
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("정상 탈퇴 흐름 - 모든 정리 로직이 호출되고 SubstituteRequest/Target/PostingApplication 이 취소된다")
    void execute_정상탈퇴_모든정리호출() {
        // given
        Long userId = 10L;
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(workspaceQueryRepository.existsActiveWorkspaceByUserId(userId)).thenReturn(false);

        WorkspaceWorker activeWorker = mock(WorkspaceWorker.class);
        when(workspaceWorkerQueryRepository.findAllActiveByUserId(userId))
            .thenReturn(List.of(activeWorker));

        WorkspaceWorkerSchedule activeSchedule = mock(WorkspaceWorkerSchedule.class);
        when(workspaceWorkerScheduleQueryRepository.findAllActivatedByUserId(userId))
            .thenReturn(List.of(activeSchedule));

        SubstituteRequest activeRequest = mock(SubstituteRequest.class);
        when(substituteRequestQueryRepository.findAllActiveByRequesterUserId(userId))
            .thenReturn(List.of(activeRequest));

        SubstituteRequestTarget pendingTarget = mock(SubstituteRequestTarget.class);
        when(substituteRequestQueryRepository.findAllPendingTargetsByUserId(userId))
            .thenReturn(List.of(pendingTarget));

        PostingApplication activeApplication = mock(PostingApplication.class);
        when(postingApplicationQueryRepository.findAllActiveByUserId(userId))
            .thenReturn(List.of(activeApplication));

        File profileImage = mock(File.class);
        when(fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.USER_PROFILE, userId.toString()))
            .thenReturn(Optional.of(profileImage));

        // when
        withdrawalUser.execute(user);

        // then
        verify(user, times(1)).withdraw();
        verify(userRepository, times(1)).save(user);
        verify(userCertificateRepository, times(1)).deleteAll(user);
        verify(activeWorker, times(1)).resign();
        verify(activeSchedule, times(1)).delete();
        verify(activeRequest, times(1)).cancel();
        verify(pendingTarget, times(1)).cancel();
        verify(activeApplication, times(1)).updateStatus(PostingApplicationStatus.CANCELLED);
        verify(profileImage, times(1)).markDeleted();
        verify(authService, times(1)).revokeAllExistingAuthorizations(user);
        verify(notificationService, times(1)).removeUserDeviceToken(user);
    }

    @Test
    @DisplayName("정리 대상이 비어있어도 정상 탈퇴 흐름이 완료된다")
    void execute_정리대상없음_정상완료() {
        // given
        Long userId = 20L;
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(workspaceQueryRepository.existsActiveWorkspaceByUserId(userId)).thenReturn(false);
        when(workspaceWorkerQueryRepository.findAllActiveByUserId(userId)).thenReturn(List.of());
        when(workspaceWorkerScheduleQueryRepository.findAllActivatedByUserId(userId)).thenReturn(List.of());
        when(substituteRequestQueryRepository.findAllActiveByRequesterUserId(userId)).thenReturn(List.of());
        when(substituteRequestQueryRepository.findAllPendingTargetsByUserId(userId)).thenReturn(List.of());
        when(postingApplicationQueryRepository.findAllActiveByUserId(userId)).thenReturn(List.of());
        when(fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.USER_PROFILE, userId.toString()))
            .thenReturn(Optional.empty());

        // when
        withdrawalUser.execute(user);

        // then
        verify(user, times(1)).withdraw();
        verify(userRepository, times(1)).save(user);
        verify(userCertificateRepository, times(1)).deleteAll(user);
        verify(authService, times(1)).revokeAllExistingAuthorizations(user);
        verify(notificationService, times(1)).removeUserDeviceToken(user);
    }
}
