package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.application.file.FileDeleteService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserQueryRepository;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserRepository;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ApproveWorkspaceRequest 테스트")
class ApproveWorkspaceRequestTest {

    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private ManagerUserQueryRepository managerUserQueryRepository;
    @Mock private ManagerUserRepository managerUserRepository;
    @Mock private WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
    @Mock private FileQueryRepository fileQueryRepository;
    @Mock private FileDeleteService fileDeleteService;
    @Mock private AuthService authService;

    @InjectMocks
    private ApproveWorkspaceRequest approveWorkspaceRequest;

    private WorkspaceRequest workspaceRequest;
    private User requester;
    private ManagerUser managerUser;

    private static final Long WORKSPACE_REQUEST_ID = 1L;
    private static final Long USER_ID = 10L;

    @BeforeEach
    void setUp() {
        requester = mock(User.class);
        given(requester.getId()).willReturn(USER_ID);

        workspaceRequest = mock(WorkspaceRequest.class);
        given(workspaceRequest.getUser()).willReturn(requester);

        managerUser = mock(ManagerUser.class);

        given(workspaceRequestQueryRepository.findByIdWithUser(WORKSPACE_REQUEST_ID))
            .willReturn(Optional.of(workspaceRequest));
        given(managerUserQueryRepository.findByUserId(USER_ID))
            .willReturn(Optional.of(managerUser));
        given(fileQueryRepository.findByTargetTypeAndTargetId(any(FileTargetType.class), any()))
            .willReturn(Optional.empty());
    }

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("존재하지 않는 요청이면 NOT_FOUND 예외 발생")
        void fails_whenNotFound() {
            // given
            given(workspaceRequestQueryRepository.findByIdWithUser(WORKSPACE_REQUEST_ID))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> approveWorkspaceRequest.execute(WORKSPACE_REQUEST_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));

            then(authService).should(never()).revokeAllExistingAuthorizations(any());
        }

        @Test
        @DisplayName("USER -> MANAGER 승급 시 role 승급 + 기존 세션 무효화 호출")
        void succeeds_promotesAndRevokesSessions() {
            // given
            given(requester.promoteToManager()).willReturn(true);

            // when
            approveWorkspaceRequest.execute(WORKSPACE_REQUEST_ID);

            // then
            then(workspaceRequest).should().approve();
            then(requester).should().promoteToManager();
            then(authService).should().revokeAllExistingAuthorizations(requester);
            then(workspaceRepository).should().save(any());
        }

        @Test
        @DisplayName("이미 매니저면 승급 없이 세션 무효화 미호출")
        void succeeds_skipsRevokeWhenAlreadyManager() {
            // given
            given(requester.promoteToManager()).willReturn(false);

            // when
            approveWorkspaceRequest.execute(WORKSPACE_REQUEST_ID);

            // then
            then(workspaceRequest).should().approve();
            then(requester).should().promoteToManager();
            then(authService).should(never()).revokeAllExistingAuthorizations(any());
            then(workspaceRepository).should().save(any());
        }
    }
}
