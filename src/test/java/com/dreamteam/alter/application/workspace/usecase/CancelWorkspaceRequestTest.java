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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelWorkspaceRequest 테스트")
class CancelWorkspaceRequestTest {

    @Mock
    private WorkspaceRequestQueryRepository workspaceRequestQueryRepository;

    @Mock
    private FileQueryRepository fileQueryRepository;

    @Mock
    private FileDeleteService fileDeleteService;

    @InjectMocks
    private CancelWorkspaceRequest cancelWorkspaceRequest;

    @Test
    @DisplayName("PENDING 신청을 취소하면 cancel 처리 후 신분증 파일을 삭제한다")
    void execute_정상취소_파일삭제() {
        // given
        User user = mock(User.class);
        WorkspaceRequest request = mock(WorkspaceRequest.class);
        File identityFile = mock(File.class);
        when(user.getId()).thenReturn(1L);
        when(request.getUser()).thenReturn(user);
        when(workspaceRequestQueryRepository.findByIdWithUser(1L)).thenReturn(Optional.of(request));
        when(fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.WORKSPACE_OWN_IDENTITY, "1"))
            .thenReturn(Optional.of(identityFile));

        // when
        cancelWorkspaceRequest.execute(user, 1L);

        // then
        verify(request).cancel();
        verify(fileDeleteService).delete(identityFile);
    }

    @Test
    @DisplayName("신분증 파일이 없으면 파일 삭제 없이 취소만 처리한다")
    void execute_파일없음_취소만() {
        // given
        User user = mock(User.class);
        WorkspaceRequest request = mock(WorkspaceRequest.class);
        when(user.getId()).thenReturn(1L);
        when(request.getUser()).thenReturn(user);
        when(workspaceRequestQueryRepository.findByIdWithUser(1L)).thenReturn(Optional.of(request));
        when(fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.WORKSPACE_OWN_IDENTITY, "1"))
            .thenReturn(Optional.empty());

        // when
        cancelWorkspaceRequest.execute(user, 1L);

        // then
        verify(request).cancel();
        verify(fileDeleteService, never()).delete(any());
    }

    @Test
    @DisplayName("존재하지 않는 신청이면 NOT_FOUND 예외가 발생한다")
    void execute_존재하지않음_예외발생() {
        // given
        User user = mock(User.class);
        when(workspaceRequestQueryRepository.findByIdWithUser(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cancelWorkspaceRequest.execute(user, 1L))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
        verify(fileDeleteService, never()).delete(any());
    }

    @Test
    @DisplayName("본인 신청이 아니면 NOT_FOUND 예외가 발생한다")
    void execute_비소유자_예외발생() {
        // given
        User requester = mock(User.class);
        User owner = mock(User.class);
        WorkspaceRequest request = mock(WorkspaceRequest.class);
        when(requester.getId()).thenReturn(1L);
        when(owner.getId()).thenReturn(2L);
        when(request.getUser()).thenReturn(owner);
        when(workspaceRequestQueryRepository.findByIdWithUser(1L)).thenReturn(Optional.of(request));

        // when & then
        assertThatThrownBy(() -> cancelWorkspaceRequest.execute(requester, 1L))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
        verify(request, never()).cancel();
        verify(fileDeleteService, never()).delete(any());
    }

    @Test
    @DisplayName("취소할 수 없는 상태면 예외가 전파되고 파일을 삭제하지 않는다")
    void execute_취소불가상태_파일삭제안함() {
        // given
        User user = mock(User.class);
        WorkspaceRequest request = mock(WorkspaceRequest.class);
        when(user.getId()).thenReturn(1L);
        when(request.getUser()).thenReturn(user);
        when(workspaceRequestQueryRepository.findByIdWithUser(1L)).thenReturn(Optional.of(request));
        doThrow(new CustomException(ErrorCode.CONFLICT, "취소할 수 없는 상태입니다.")).when(request).cancel();

        // when & then
        assertThatThrownBy(() -> cancelWorkspaceRequest.execute(user, 1L))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));
        verify(fileDeleteService, never()).delete(any());
    }
}
