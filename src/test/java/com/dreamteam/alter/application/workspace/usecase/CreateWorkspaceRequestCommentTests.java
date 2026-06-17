package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequestComment;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestCommentRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.type.CommentOwner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@DisplayName("CreateWorkspaceRequestComment 테스트")
class CreateWorkspaceRequestCommentTests {

    @Mock
    private WorkspaceRequestQueryRepository workspaceRequestQueryRepository;

    @Mock
    private WorkspaceRequestCommentRepository workspaceRequestCommentRepository;

    @Mock
    private AttachFilesUseCase attachFiles;

    @InjectMocks
    private CreateWorkspaceRequestComment createWorkspaceRequestComment;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("존재하지 않는 신청이면 NOT_FOUND 예외가 발생하고 댓글을 저장하지 않는다")
        void execute_존재하지않음_예외발생() {
            // given
            User user = mock(User.class);
            given(workspaceRequestQueryRepository.findByIdWithUser(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> createWorkspaceRequestComment.execute(user, 1L, CommentOwner.USER, "내용", null))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
            then(workspaceRequestCommentRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("USER 가 본인 신청이 아니면 FORBIDDEN 예외가 발생하고 댓글을 저장하지 않는다")
        void execute_USER_비소유자_예외발생() {
            // given
            User requester = mock(User.class);
            User owner = mock(User.class);
            WorkspaceRequest request = mock(WorkspaceRequest.class);
            given(requester.getId()).willReturn(1L);
            given(owner.getId()).willReturn(2L);
            given(request.getUser()).willReturn(owner);
            given(workspaceRequestQueryRepository.findByIdWithUser(1L)).willReturn(Optional.of(request));

            // when & then
            assertThatThrownBy(() -> createWorkspaceRequestComment.execute(requester, 1L, CommentOwner.USER, "내용", null))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
            then(workspaceRequestCommentRepository).should(never()).save(any());
            then(attachFiles).should(never()).execute(any(), any(), any(), any());
        }

        @Test
        @DisplayName("USER 본인 신청이면 댓글을 저장하고 첨부파일이 있으면 attachFiles 를 호출한다")
        void execute_USER_소유자_파일첨부() {
            // given
            User user = mock(User.class);
            WorkspaceRequest request = mock(WorkspaceRequest.class);
            WorkspaceRequestComment saved = mock(WorkspaceRequestComment.class);
            List<String> fileIds = List.of("f1", "f2");
            given(user.getId()).willReturn(1L);
            given(request.getUser()).willReturn(user);
            given(workspaceRequestQueryRepository.findByIdWithUser(1L)).willReturn(Optional.of(request));
            given(workspaceRequestCommentRepository.save(any(WorkspaceRequestComment.class))).willReturn(saved);
            given(saved.getId()).willReturn(100L);

            // when
            createWorkspaceRequestComment.execute(user, 1L, CommentOwner.USER, "내용", fileIds);

            // then
            then(workspaceRequestCommentRepository).should().save(any(WorkspaceRequestComment.class));
            then(attachFiles).should().execute(fileIds, FileTargetType.WORKSPACE_REQUEST_COMMENT, "100", 1L);
        }

        @Test
        @DisplayName("첨부파일이 없으면 댓글만 저장하고 attachFiles 는 호출하지 않는다")
        void execute_파일없음_댓글만저장() {
            // given
            User user = mock(User.class);
            WorkspaceRequest request = mock(WorkspaceRequest.class);
            given(user.getId()).willReturn(1L);
            given(request.getUser()).willReturn(user);
            given(workspaceRequestQueryRepository.findByIdWithUser(1L)).willReturn(Optional.of(request));
            given(workspaceRequestCommentRepository.save(any(WorkspaceRequestComment.class)))
                .willReturn(mock(WorkspaceRequestComment.class));

            // when
            createWorkspaceRequestComment.execute(user, 1L, CommentOwner.USER, "내용", List.of());

            // then
            then(workspaceRequestCommentRepository).should().save(any(WorkspaceRequestComment.class));
            then(attachFiles).should(never()).execute(any(), any(), any(), any());
        }

        @Test
        @DisplayName("ADMIN 은 소유권 검증 없이 타인 신청에도 댓글을 저장한다")
        void execute_ADMIN_소유권무관_저장() {
            // given
            User admin = mock(User.class);
            WorkspaceRequest request = mock(WorkspaceRequest.class);
            given(workspaceRequestQueryRepository.findByIdWithUser(1L)).willReturn(Optional.of(request));
            given(workspaceRequestCommentRepository.save(any(WorkspaceRequestComment.class)))
                .willReturn(mock(WorkspaceRequestComment.class));

            // when
            createWorkspaceRequestComment.execute(admin, 1L, CommentOwner.ADMIN, "내용", null);

            // then
            then(workspaceRequestCommentRepository).should().save(any(WorkspaceRequestComment.class));
            then(request).should(never()).getUser();
        }
    }
}
