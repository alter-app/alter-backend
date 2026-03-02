package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.LoginWithPasswordRequestDto;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.type.UserRole;
import com.dreamteam.alter.domain.user.type.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginWithPassword 테스트")
class LoginWithPasswordTests {

    @Mock
    private UserQueryRepository userQueryRepository;

    @Mock
    private AuthService authService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginWithPassword loginWithPassword;

    private LoginWithPasswordRequestDto request;

    @BeforeEach
    void setUp() {
        request = new LoginWithPasswordRequestDto("test@example.com", "password123!");
    }

    private User createMockUser(UserStatus status, UserRole role, String encodedPassword) {
        User user = mock(User.class);
        given(user.getStatus()).willReturn(status);
        given(user.getRole()).willReturn(role);
        given(user.getPassword()).willReturn(encodedPassword);
        return user;
    }

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 INVALID_LOGIN_INFO 예외 발생")
        void fails_whenEmailNotFound() {
            // given
            given(userQueryRepository.findByEmail("test@example.com")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> loginWithPassword.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.INVALID_LOGIN_INFO);
                });

            then(passwordEncoder).shouldHaveNoInteractions();
            then(authService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("비밀번호 불일치 시 INVALID_LOGIN_INFO 예외 발생")
        void fails_whenPasswordNotMatch() {
            // given
            User user = mock(User.class);
            given(user.getPassword()).willReturn("encodedPassword");
            given(userQueryRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("password123!", "encodedPassword")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> loginWithPassword.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.INVALID_LOGIN_INFO);
                });

            then(authService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("SUSPENDED 사용자 로그인 시 SUSPENDED_USER 예외 발생")
        void fails_whenUserIsSuspended() {
            // given
            User user = mock(User.class);
            given(user.getPassword()).willReturn("encodedPassword");
            given(user.getStatus()).willReturn(UserStatus.SUSPENDED);
            given(userQueryRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("password123!", "encodedPassword")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> loginWithPassword.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.SUSPENDED_USER);
                });

            then(authService).should(never()).revokeAllExistingAuthorizations(any());
            then(authService).should(never()).generateAuthorization(any(), any());
        }

        @Test
        @DisplayName("DELETED 사용자 로그인 시 DELETED_USER 예외 발생")
        void fails_whenUserIsDeleted() {
            // given
            User user = mock(User.class);
            given(user.getPassword()).willReturn("encodedPassword");
            given(user.getStatus()).willReturn(UserStatus.DELETED);
            given(userQueryRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("password123!", "encodedPassword")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> loginWithPassword.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.DELETED_USER);
                });

            then(authService).should(never()).revokeAllExistingAuthorizations(any());
            then(authService).should(never()).generateAuthorization(any(), any());
        }

        @Test
        @DisplayName("ACTIVE 사용자 로그인 성공")
        void succeeds_whenUserIsActive() {
            // given
            User user = createMockUser(UserStatus.ACTIVE, UserRole.ROLE_USER, "encodedPassword");
            Authorization authorization = mock(Authorization.class);

            given(userQueryRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("password123!", "encodedPassword")).willReturn(true);
            given(authService.generateAuthorization(user, TokenScope.APP)).willReturn(authorization);

            // when
            GenerateTokenResponseDto result = loginWithPassword.execute(request);

            // then
            assertThat(result).isNotNull();
            then(authService).should().revokeAllExistingAuthorizations(user);
            then(authService).should().generateAuthorization(user, TokenScope.APP);
        }

        @Test
        @DisplayName("ADMIN 역할 사용자 로그인 시 ADMIN scope 토큰 발급")
        void succeeds_withAdminScope_whenUserIsAdmin() {
            // given
            User user = createMockUser(UserStatus.ACTIVE, UserRole.ROLE_ADMIN, "encodedPassword");
            Authorization authorization = mock(Authorization.class);

            given(userQueryRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("password123!", "encodedPassword")).willReturn(true);
            given(authService.generateAuthorization(user, TokenScope.ADMIN)).willReturn(authorization);

            // when
            GenerateTokenResponseDto result = loginWithPassword.execute(request);

            // then
            assertThat(result).isNotNull();
            then(authService).should().generateAuthorization(user, TokenScope.ADMIN);
        }

        @Test
        @DisplayName("MANAGER 역할 사용자 로그인 시 MANAGER scope 토큰 발급")
        void succeeds_withManagerScope_whenUserIsManager() {
            // given
            User user = createMockUser(UserStatus.ACTIVE, UserRole.ROLE_MANAGER, "encodedPassword");
            Authorization authorization = mock(Authorization.class);

            given(userQueryRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("password123!", "encodedPassword")).willReturn(true);
            given(authService.generateAuthorization(user, TokenScope.MANAGER)).willReturn(authorization);

            // when
            GenerateTokenResponseDto result = loginWithPassword.execute(request);

            // then
            assertThat(result).isNotNull();
            then(authService).should().generateAuthorization(user, TokenScope.MANAGER);
        }
    }
}
