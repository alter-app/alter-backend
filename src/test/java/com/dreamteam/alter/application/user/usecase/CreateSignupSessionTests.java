package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateSignupSessionRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateSignupSessionResponseDto;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateSignupSession 테스트")
class CreateSignupSessionTests {

    @Mock
    private UserQueryRepository userQueryRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private FirebaseTokenVerifier firebaseTokenVerifier;

    @InjectMocks
    private CreateSignupSession createSignupSession;

    @Test
    @DisplayName("유효한 토큰으로 세션 생성 성공")
    void execute_유효한토큰_세션생성성공() {
        // given
        String firebaseIdToken = "valid-firebase-token";
        CreateSignupSessionRequestDto request = new CreateSignupSessionRequestDto(firebaseIdToken);

        when(firebaseTokenVerifier.verifyAndGetPhoneNumber(firebaseIdToken)).thenReturn("+821012345678");
        when(userQueryRepository.findByContact("01012345678")).thenReturn(Optional.empty());

        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("SIGNUP:CONTACT:01012345678")).thenReturn(null);

        // when
        CreateSignupSessionResponseDto result = createSignupSession.execute(request);

        // then
        assertNotNull(result);
        assertNotNull(result.getSignupSessionId());
        verify(firebaseTokenVerifier).verifyAndGetPhoneNumber(firebaseIdToken);
        verify(userQueryRepository).findByContact("01012345678");
    }

    @Test
    @DisplayName("기존 세션이 있을 때 삭제 후 새 세션 생성 성공")
    void execute_기존세션존재_삭제후생성성공() {
        // given
        String firebaseIdToken = "valid-firebase-token";
        String existingSessionId = "existing-session-id";
        CreateSignupSessionRequestDto request = new CreateSignupSessionRequestDto(firebaseIdToken);

        when(firebaseTokenVerifier.verifyAndGetPhoneNumber(firebaseIdToken)).thenReturn("+821012345678");
        when(userQueryRepository.findByContact("01012345678")).thenReturn(Optional.empty());

        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("SIGNUP:CONTACT:01012345678")).thenReturn(existingSessionId);

        // when
        CreateSignupSessionResponseDto result = createSignupSession.execute(request);

        // then
        assertNotNull(result);
        assertNotNull(result.getSignupSessionId());
        verify(redisTemplate).delete("SIGNUP:PENDING:" + existingSessionId);
        verify(redisTemplate).delete("SIGNUP:CONTACT:01012345678");
    }

    @Test
    @DisplayName("유효하지 않은 Firebase 토큰으로 UNAUTHORIZED 예외 발생")
    void execute_유효하지않은토큰_예외발생() {
        // given
        String firebaseIdToken = "invalid-firebase-token";
        CreateSignupSessionRequestDto request = new CreateSignupSessionRequestDto(firebaseIdToken);

        when(firebaseTokenVerifier.verifyAndGetPhoneNumber(firebaseIdToken))
            .thenThrow(new CustomException(ErrorCode.UNAUTHORIZED, "Firebase 인증 토큰이 유효하지 않습니다."));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            createSignupSession.execute(request);
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        verify(userQueryRepository, never()).findByContact(anyString());
    }

    @Test
    @DisplayName("이미 사용 중인 휴대폰 번호로 USER_CONTACT_DUPLICATED 예외 발생")
    void execute_중복번호_예외발생() {
        // given
        String firebaseIdToken = "valid-firebase-token";
        CreateSignupSessionRequestDto request = new CreateSignupSessionRequestDto(firebaseIdToken);

        when(firebaseTokenVerifier.verifyAndGetPhoneNumber(firebaseIdToken)).thenReturn("+821012345678");

        User existingUser = mock(User.class);
        when(userQueryRepository.findByContact("01012345678")).thenReturn(Optional.of(existingUser));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            createSignupSession.execute(request);
        });

        assertEquals(ErrorCode.USER_CONTACT_DUPLICATED, exception.getErrorCode());
    }
}
