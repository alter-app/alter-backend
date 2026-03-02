package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreatePasswordResetSessionRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreatePasswordResetSessionResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.port.outbound.FirebaseTokenVerifier;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePasswordResetSession 테스트")
class CreatePasswordResetSessionTests {

    @Mock
    private UserQueryRepository userQueryRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private FirebaseTokenVerifier firebaseTokenVerifier;

    @InjectMocks
    private CreatePasswordResetSession createPasswordResetSession;

    @Test
    @DisplayName("유효한 토큰으로 세션 생성 성공")
    void execute_유효한토큰_세션생성성공() {
        // given
        String firebaseIdToken = "valid-firebase-token";
        CreatePasswordResetSessionRequestDto request = new CreatePasswordResetSessionRequestDto(firebaseIdToken);

        when(firebaseTokenVerifier.verifyAndGetPhoneNumber(firebaseIdToken)).thenReturn("+821012345678");

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userQueryRepository.findByContact("01012345678")).thenReturn(Optional.of(user));

        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("PASSWORD_RESET:USER:1")).thenReturn(null);

        // when
        CreatePasswordResetSessionResponseDto result = createPasswordResetSession.execute(request);

        // then
        assertNotNull(result);
        assertNotNull(result.getSessionId());
        verify(firebaseTokenVerifier).verifyAndGetPhoneNumber(firebaseIdToken);
        verify(userQueryRepository).findByContact("01012345678");
    }

    @Test
    @DisplayName("유효하지 않은 Firebase 토큰으로 UNAUTHORIZED 예외 발생")
    void execute_유효하지않은토큰_예외발생() {
        // given
        String firebaseIdToken = "invalid-firebase-token";
        CreatePasswordResetSessionRequestDto request = new CreatePasswordResetSessionRequestDto(firebaseIdToken);

        when(firebaseTokenVerifier.verifyAndGetPhoneNumber(firebaseIdToken))
            .thenThrow(new CustomException(ErrorCode.UNAUTHORIZED, "Firebase 인증 토큰이 유효하지 않습니다."));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            createPasswordResetSession.execute(request);
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        verify(userQueryRepository, never()).findByContact(anyString());
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 USER_NOT_FOUND 예외 발생")
    void execute_사용자없음_예외발생() {
        // given
        String firebaseIdToken = "valid-firebase-token";
        CreatePasswordResetSessionRequestDto request = new CreatePasswordResetSessionRequestDto(firebaseIdToken);

        when(firebaseTokenVerifier.verifyAndGetPhoneNumber(firebaseIdToken)).thenReturn("+821012345678");
        when(userQueryRepository.findByContact("01012345678")).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            createPasswordResetSession.execute(request);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }
}
