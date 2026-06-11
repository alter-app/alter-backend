package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.application.file.FileDeleteService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CancelWorkspaceRequest 테스트")
class CancelWorkspaceRequestTest {

    @Mock private WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
    @Mock private FileQueryRepository fileQueryRepository;
    @Mock private FileDeleteService fileDeleteService;

    @InjectMocks
    private CancelWorkspaceRequest cancelWorkspaceRequest;

    private User user;
    private WorkspaceRequest workspaceRequest;

    private static final Long WORKSPACE_REQUEST_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final String TARGET_ID = String.valueOf(WORKSPACE_REQUEST_ID);

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        given(user.getId()).willReturn(USER_ID);

        workspaceRequest = mock(WorkspaceRequest.class);

        given(workspaceRequestQueryRepository.existsByIdAndUserId(WORKSPACE_REQUEST_ID, USER_ID))
            .willReturn(true);
        given(workspaceRequestQueryRepository.findByIdWithUser(WORKSPACE_REQUEST_ID))
            .willReturn(Optional.of(workspaceRequest));
        given(fileQueryRepository.findByTargetTypeAndTargetId(any(FileTargetType.class), any()))
            .willReturn(Optional.empty());
    }

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("본인 요청이 아니면 FORBIDDEN 예외 발생")
        void fails_whenNotOwner() {
            // given
            given(workspaceRequestQueryRepository.existsByIdAndUserId(WORKSPACE_REQUEST_ID, USER_ID))
                .willReturn(false);

            // when & then
            assertThatThrownBy(() -> cancelWorkspaceRequest.execute(user, WORKSPACE_REQUEST_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

            then(workspaceRequest).should(never()).cancel();
            then(fileDeleteService).should(never()).delete(any());
        }

        @Test
        @DisplayName("취소 불가능한 상태(예: ACTIVATED)면 CONFLICT 예외 발생")
        void fails_whenNotCancelableState() {
            // given
            willThrow(new CustomException(ErrorCode.CONFLICT, "취소할 수 없는 상태의 요청입니다."))
                .given(workspaceRequest).cancel();

            // when & then
            assertThatThrownBy(() -> cancelWorkspaceRequest.execute(user, WORKSPACE_REQUEST_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

            then(fileDeleteService).should(never()).delete(any());
        }

        @Test
        @DisplayName("정상 취소 시 cancel 호출 및 첨부 파일 3종 삭제")
        void succeeds_cancelsAndDeletesFiles() {
            // given
            File certFile = mock(File.class);
            File ownIdentityFile = mock(File.class);
            File warrantFile = mock(File.class);
            given(fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.WORKSPACE_CERTIFICATE, TARGET_ID))
                .willReturn(Optional.of(certFile));
            given(fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.WORKSPACE_OWN_IDENTITY, TARGET_ID))
                .willReturn(Optional.of(ownIdentityFile));
            given(fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.WORKSPACE_WARRANT, TARGET_ID))
                .willReturn(Optional.of(warrantFile));

            // when
            cancelWorkspaceRequest.execute(user, WORKSPACE_REQUEST_ID);

            // then
            then(workspaceRequest).should().cancel();
            then(fileDeleteService).should().delete(certFile);
            then(fileDeleteService).should().delete(ownIdentityFile);
            then(fileDeleteService).should().delete(warrantFile);
        }

        @Test
        @DisplayName("위임장 파일이 없어도 나머지 파일만 삭제하고 정상 취소")
        void succeeds_whenWarrantFileMissing() {
            // given
            File certFile = mock(File.class);
            File ownIdentityFile = mock(File.class);
            given(fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.WORKSPACE_CERTIFICATE, TARGET_ID))
                .willReturn(Optional.of(certFile));
            given(fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.WORKSPACE_OWN_IDENTITY, TARGET_ID))
                .willReturn(Optional.of(ownIdentityFile));
            given(fileQueryRepository.findByTargetTypeAndTargetId(eq(FileTargetType.WORKSPACE_WARRANT), any()))
                .willReturn(Optional.empty());

            // when
            cancelWorkspaceRequest.execute(user, WORKSPACE_REQUEST_ID);

            // then
            then(workspaceRequest).should().cancel();
            then(fileDeleteService).should(times(2)).delete(any());
        }
    }
}
