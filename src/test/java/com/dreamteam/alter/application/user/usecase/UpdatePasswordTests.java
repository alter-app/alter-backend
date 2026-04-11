package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.UpdatePasswordRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdatePassword 테스트")
class UpdatePasswordTests {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UpdatePassword updatePassword;

    private AppActor actor;
    private User user;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        actor = mock(AppActor.class);
        given(actor.getUser()).willReturn(user);
    }

    @Nested
    @DisplayName("비밀번호가 있는 사용자")
    class UserWithPassword {

        @BeforeEach
        void setUp() {
            given(user.getPassword()).willReturn("encodedCurrentPassword");
        }

        @Test
        @DisplayName("올바른 현재 비밀번호와 유효한 새 비밀번호로 변경 성공")
        void execute_withCorrectCurrentPassword_succeeds() {
            // given
            UpdatePasswordRequestDto request = new UpdatePasswordRequestDto("currentPass1!", "newPass1!");
            given(passwordEncoder.matches("currentPass1!", "encodedCurrentPassword")).willReturn(true);
            given(passwordEncoder.encode("newPass1!")).willReturn("encodedNewPassword");

            // when & then
            assertThatNoException().isThrownBy(() -> updatePassword.execute(actor, request));
            then(user).should().updatePassword("encodedNewPassword");
        }

        @Test
        @DisplayName("틀린 현재 비밀번호 입력 시 INVALID_CURRENT_PASSWORD 예외 발생")
        void execute_withWrongCurrentPassword_throwsException() {
            // given
            UpdatePasswordRequestDto request = new UpdatePasswordRequestDto("wrongPass1!", "newPass1!");
            given(passwordEncoder.matches("wrongPass1!", "encodedCurrentPassword")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> updatePassword.execute(actor, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CURRENT_PASSWORD);
            then(user).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("현재 비밀번호를 null로 전송 시 INVALID_CURRENT_PASSWORD 예외 발생")
        void execute_withNullCurrentPassword_throwsException() {
            // given
            UpdatePasswordRequestDto request = new UpdatePasswordRequestDto(null, "newPass1!");

            // when & then
            assertThatThrownBy(() -> updatePassword.execute(actor, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CURRENT_PASSWORD);
            then(user).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("유효하지 않은 새 비밀번호 형식으로 변경 시 INVALID_PASSWORD_FORMAT 예외 발생")
        void execute_withInvalidNewPasswordFormat_throwsException() {
            // given
            UpdatePasswordRequestDto request = new UpdatePasswordRequestDto("currentPass1!", "short");
            given(passwordEncoder.matches("currentPass1!", "encodedCurrentPassword")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> updatePassword.execute(actor, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD_FORMAT);
            then(user).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("비밀번호가 없는 소셜 전용 사용자")
    class SocialOnlyUser {

        @BeforeEach
        void setUp() {
            given(user.getPassword()).willReturn(null);
        }

        @Test
        @DisplayName("currentPassword 없이 유효한 새 비밀번호만으로 설정 성공")
        void execute_withoutCurrentPassword_succeeds() {
            // given
            UpdatePasswordRequestDto request = new UpdatePasswordRequestDto(null, "newPass1!");
            given(passwordEncoder.encode("newPass1!")).willReturn("encodedNewPassword");

            // when & then
            assertThatNoException().isThrownBy(() -> updatePassword.execute(actor, request));
            then(user).should().updatePassword("encodedNewPassword");
            then(passwordEncoder).should(org.mockito.Mockito.never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("유효하지 않은 새 비밀번호 형식으로 설정 시 INVALID_PASSWORD_FORMAT 예외 발생")
        void execute_withInvalidNewPasswordFormat_throwsException() {
            // given
            UpdatePasswordRequestDto request = new UpdatePasswordRequestDto(null, "weak");

            // when & then
            assertThatThrownBy(() -> updatePassword.execute(actor, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD_FORMAT);
            then(user).shouldHaveNoMoreInteractions();
        }
    }
}
