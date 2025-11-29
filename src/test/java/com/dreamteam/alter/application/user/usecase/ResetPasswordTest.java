package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.ResetPasswordRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.type.UserGender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResetPassword 테스트")
class ResetPasswordTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private UserQueryRepository userQueryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ResetPassword resetPassword;

    @Test
    @DisplayName("Redis에 세션이 없을 때 PASSWORD_RESET_SESSION_NOT_EXIST 예외 발생")
    void execute_세션없음_예외발생() {
        // given
        ResetPasswordRequestDto request = new ResetPasswordRequestDto("session-id", "newPassword123");
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            resetPassword.execute(request);
        });

        assertEquals(ErrorCode.PASSWORD_RESET_SESSION_NOT_EXIST, exception.getErrorCode());
        verify(redisTemplate, times(1)).opsForValue();
        verify(userQueryRepository, never()).findById(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("세션은 있으나 사용자가 없을 때 USER_NOT_FOUND 예외 발생")
    void execute_사용자없음_예외발생() {
        // given
        ResetPasswordRequestDto request = new ResetPasswordRequestDto("session-id", "newPassword123");
        String userId = "1";
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(userId);
        when(userQueryRepository.findById(Long.parseLong(userId))).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            resetPassword.execute(request);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(redisTemplate, times(1)).opsForValue();
        verify(userQueryRepository, times(1)).findById(Long.parseLong(userId));
        verify(passwordEncoder, never()).encode(anyString());
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("정상적인 비밀번호 재설정 - 비밀번호 업데이트 및 Redis 키 삭제 확인")
    void execute_정상적인비밀번호재설정() {
        // given
        String sessionId = "session-id";
        String newPassword = "newPassword123";
        String encodedPassword = "encodedPassword123";
        String userId = "1";
        Long userIdLong = Long.parseLong(userId);

        ResetPasswordRequestDto request = new ResetPasswordRequestDto(sessionId, newPassword);
        User user = User.create(
            "test@example.com",
            "01012345678",
            "oldPassword",
            "테스트",
            "testuser",
            UserGender.GENDER_MALE,
            "19900101"
        );

        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        String sessionKey = "PASSWORD_RESET:PENDING:" + sessionId;
        String userIndexKey = "PASSWORD_RESET:USER:" + userId;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(sessionKey)).thenReturn(userId);
        when(userQueryRepository.findById(userIdLong)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        // when
        resetPassword.execute(request);

        // then
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).get(sessionKey);
        verify(userQueryRepository, times(1)).findById(userIdLong);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(redisTemplate, times(1)).delete(sessionKey);
        verify(redisTemplate, times(1)).delete(userIndexKey);
        assertEquals(encodedPassword, user.getPassword());
    }
}
