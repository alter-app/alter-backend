package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUpdateUserStatusRequestDto;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AdminActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.AdminUserQueryRepository;
import com.dreamteam.alter.domain.user.type.UserStatus;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUpdateUserStatus 테스트")
class AdminUpdateUserStatusTests {

    @Mock
    private AdminUserQueryRepository adminUserQueryRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AdminUpdateUserStatus adminUpdateUserStatus;

    private AdminActor actor;

    @BeforeEach
    void setUp() {
        actor = mock(AdminActor.class);
    }

    private User createMockUser(UserStatus status) {
        User user = mock(User.class);
        given(user.getStatus()).willReturn(status);
        return user;
    }

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("DELETED 상태로 변경 시도 시 ILLEGAL_ARGUMENT 예외 발생")
        void fails_whenStatusIsDeleted() {
            // given
            Long userId = 1L;
            AdminUpdateUserStatusRequestDto request = new AdminUpdateUserStatusRequestDto(UserStatus.DELETED);

            // when & then
            assertThatThrownBy(() -> adminUpdateUserStatus.execute(userId, request, actor))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
                });

            then(adminUserQueryRepository).shouldHaveNoInteractions();
            then(authService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("존재하지 않는 사용자일 경우 NOT_FOUND 예외 발생")
        void fails_whenUserNotFound() {
            // given
            Long userId = 1L;
            AdminUpdateUserStatusRequestDto request = new AdminUpdateUserStatusRequestDto(UserStatus.SUSPENDED);
            given(adminUserQueryRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminUpdateUserStatus.execute(userId, request, actor))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                });

            then(authService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("현재 상태와 동일한 상태로 변경 시도 시 CONFLICT 예외 발생")
        void fails_whenSameStatus() {
            // given
            Long userId = 1L;
            AdminUpdateUserStatusRequestDto request = new AdminUpdateUserStatusRequestDto(UserStatus.SUSPENDED);
            User user = mock(User.class);
            given(adminUserQueryRepository.findById(userId)).willReturn(Optional.of(user));
            willThrow(new IllegalArgumentException()).given(user).updateStatus(UserStatus.SUSPENDED);

            // when & then
            assertThatThrownBy(() -> adminUpdateUserStatus.execute(userId, request, actor))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
                });

            then(authService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("SUSPENDED로 상태 변경 시 기존 인가 정보 revoke 호출")
        void succeeds_andRevokesAuthorizations_whenStatusIsSuspended() {
            // given
            Long userId = 1L;
            AdminUpdateUserStatusRequestDto request = new AdminUpdateUserStatusRequestDto(UserStatus.SUSPENDED);
            User user = mock(User.class);
            given(adminUserQueryRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            adminUpdateUserStatus.execute(userId, request, actor);

            // then
            then(user).should().updateStatus(UserStatus.SUSPENDED);
            then(authService).should().revokeAllExistingAuthorizations(user);
        }

        @Test
        @DisplayName("ACTIVE로 상태 변경 시 인가 정보 revoke 호출하지 않음")
        void succeeds_withoutRevoke_whenStatusIsActive() {
            // given
            Long userId = 1L;
            AdminUpdateUserStatusRequestDto request = new AdminUpdateUserStatusRequestDto(UserStatus.ACTIVE);
            User user = mock(User.class);
            given(adminUserQueryRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            adminUpdateUserStatus.execute(userId, request, actor);

            // then
            then(user).should().updateStatus(UserStatus.ACTIVE);
            then(authService).should(never()).revokeAllExistingAuthorizations(user);
        }
    }
}
