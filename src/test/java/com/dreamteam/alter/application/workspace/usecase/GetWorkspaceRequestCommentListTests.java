package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestCommentResponseDto;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestCommentQueryRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetWorkspaceRequestCommentList 테스트")
class GetWorkspaceRequestCommentListTests {

    @Mock
    private WorkspaceRequestQueryRepository workspaceRequestQueryRepository;

    @Mock
    private WorkspaceRequestCommentQueryRepository workspaceRequestCommentQueryRepository;

    @Mock
    private FileQueryRepository fileQueryRepository;

    @Mock
    private FileUrlService fileUrlService;

    @InjectMocks
    private GetWorkspaceRequestCommentList getWorkspaceRequestCommentList;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("USER 가 본인 신청이 아니면 FORBIDDEN 예외가 발생하고 댓글을 조회하지 않는다")
        void execute_USER_비소유자_예외발생() {
            // given
            User requester = mock(User.class);
            given(requester.getId()).willReturn(1L);
            given(workspaceRequestQueryRepository.existsByIdAndUserId(1L, 1L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> getWorkspaceRequestCommentList.execute(1L, requester, CommentOwner.USER))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
            then(workspaceRequestCommentQueryRepository).should(never()).getCommentList(any());
        }

        @Test
        @DisplayName("USER 본인 신청이면 댓글 목록을 반환한다")
        void execute_USER_소유자_목록반환() {
            // given
            User requester = mock(User.class);
            given(requester.getId()).willReturn(1L);
            given(workspaceRequestQueryRepository.existsByIdAndUserId(1L, 1L)).willReturn(true);
            given(workspaceRequestCommentQueryRepository.getCommentList(1L)).willReturn(List.of());

            // when
            List<WorkspaceRequestCommentResponseDto> result =
                getWorkspaceRequestCommentList.execute(1L, requester, CommentOwner.USER);

            // then
            assertThat(result).isEmpty();
            then(workspaceRequestCommentQueryRepository).should().getCommentList(1L);
            then(fileQueryRepository).should(never()).findAllByTargetTypeAndTargetIdIn(any(), any());
        }

        @Test
        @DisplayName("ADMIN 은 존재하지 않는 신청이면 NOT_FOUND 예외가 발생한다")
        void execute_ADMIN_존재하지않음_예외발생() {
            // given
            User admin = mock(User.class);
            given(workspaceRequestQueryRepository.existsById(1L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> getWorkspaceRequestCommentList.execute(1L, admin, CommentOwner.ADMIN))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
            then(workspaceRequestCommentQueryRepository).should(never()).getCommentList(any());
        }

        @Test
        @DisplayName("ADMIN 은 소유권 검증 없이 댓글 목록을 반환한다")
        void execute_ADMIN_소유권무관_목록반환() {
            // given
            User admin = mock(User.class);
            given(workspaceRequestQueryRepository.existsById(1L)).willReturn(true);
            given(workspaceRequestCommentQueryRepository.getCommentList(1L)).willReturn(List.of());

            // when
            List<WorkspaceRequestCommentResponseDto> result =
                getWorkspaceRequestCommentList.execute(1L, admin, CommentOwner.ADMIN);

            // then
            assertThat(result).isEmpty();
            then(workspaceRequestQueryRepository).should(never()).existsByIdAndUserId(any(), any());
            then(fileQueryRepository).should(never()).findAllByTargetTypeAndTargetIdIn(any(), any());
        }
    }
}
