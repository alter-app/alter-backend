package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.application.file.FileDeleteService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserQueryRepository;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserRepository;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceRequestStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateWorkspaceRequestStatus 테스트")
class UpdateWorkspaceRequestStatusTests {

    @Mock
    private WorkspaceRequestQueryRepository workspaceRequestQueryRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private ManagerUserQueryRepository managerUserQueryRepository;

    @Mock
    private ManagerUserRepository managerUserRepository;

    @Mock
    private FileQueryRepository fileQueryRepository;

    @Mock
    private FileDeleteService fileDeleteService;

    @InjectMocks
    private UpdateWorkspaceRequestStatus updateWorkspaceRequestStatus;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("PENDING 으로 변경 요청하면 ILLEGAL_ARGUMENT 예외가 발생하고 신청을 조회하지 않는다")
        void execute_PENDING_예외발생() {
            // when & then
            assertThatThrownBy(() -> updateWorkspaceRequestStatus.execute(1L, WorkspaceRequestStatus.PENDING))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT));
            then(workspaceRequestQueryRepository).should(never()).findByIdWithUser(any());
        }

        @Test
        @DisplayName("CANCELLED 로 변경 요청하면 ILLEGAL_ARGUMENT 예외가 발생한다")
        void execute_CANCELLED_예외발생() {
            // when & then
            assertThatThrownBy(() -> updateWorkspaceRequestStatus.execute(1L, WorkspaceRequestStatus.CANCELLED))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT));
        }

        @Test
        @DisplayName("존재하지 않는 신청이면 NOT_FOUND 예외가 발생한다")
        void execute_존재하지않음_예외발생() {
            // given
            given(workspaceRequestQueryRepository.findByIdWithUser(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> updateWorkspaceRequestStatus.execute(1L, WorkspaceRequestStatus.ACTIVATED))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
            then(workspaceRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("ACTIVATED 면 기존 ManagerUser 를 재사용해 업장을 생성하고 신분증 파일을 삭제한다")
        void execute_ACTIVATED_기존매니저_승인() {
            // given
            User user = mock(User.class);
            WorkspaceRequest request = mock(WorkspaceRequest.class);
            ManagerUser managerUser = mock(ManagerUser.class);
            File identityFile = mock(File.class);
            given(request.getUser()).willReturn(user);
            given(user.getId()).willReturn(1L);
            given(workspaceRequestQueryRepository.findByIdWithUser(1L)).willReturn(Optional.of(request));
            given(managerUserQueryRepository.findByUserId(1L)).willReturn(Optional.of(managerUser));
            given(fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.WORKSPACE_OWN_IDENTITY, "1"))
                .willReturn(Optional.of(identityFile));

            // when
            updateWorkspaceRequestStatus.execute(1L, WorkspaceRequestStatus.ACTIVATED);

            // then
            then(request).should().approve();
            then(managerUserRepository).should(never()).save(any());
            then(workspaceRepository).should().save(any(Workspace.class));
            then(fileDeleteService).should().delete(identityFile);
        }

        @Test
        @DisplayName("ACTIVATED 인데 ManagerUser 가 없으면 새로 생성해 저장한다")
        void execute_ACTIVATED_신규매니저_승인() {
            // given
            User user = mock(User.class);
            WorkspaceRequest request = mock(WorkspaceRequest.class);
            given(request.getUser()).willReturn(user);
            given(user.getId()).willReturn(1L);
            given(workspaceRequestQueryRepository.findByIdWithUser(1L)).willReturn(Optional.of(request));
            given(managerUserQueryRepository.findByUserId(1L)).willReturn(Optional.empty());
            given(fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.WORKSPACE_OWN_IDENTITY, "1"))
                .willReturn(Optional.empty());

            // when
            updateWorkspaceRequestStatus.execute(1L, WorkspaceRequestStatus.ACTIVATED);

            // then
            then(request).should().approve();
            then(managerUserRepository).should().save(any(ManagerUser.class));
            then(workspaceRepository).should().save(any(Workspace.class));
            then(fileDeleteService).should(never()).delete(any());
        }

        @Test
        @DisplayName("REVOKED 면 reject 만 처리하고 업장/매니저는 생성하지 않는다")
        void execute_REVOKED_반려() {
            // given
            WorkspaceRequest request = mock(WorkspaceRequest.class);
            given(workspaceRequestQueryRepository.findByIdWithUser(1L)).willReturn(Optional.of(request));

            // when
            updateWorkspaceRequestStatus.execute(1L, WorkspaceRequestStatus.REVOKED);

            // then
            then(request).should().reject();
            then(workspaceRepository).should(never()).save(any());
            then(managerUserRepository).should(never()).save(any());
            then(fileDeleteService).should(never()).delete(any());
        }
    }
}
