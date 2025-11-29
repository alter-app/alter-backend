package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreatePasswordResetSessionRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreatePasswordResetSessionResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.CreatePasswordResetSessionUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service("createPasswordResetSession")
@RequiredArgsConstructor
@Transactional
public class CreatePasswordResetSession implements CreatePasswordResetSessionUseCase {

    private static final String SESSION_KEY_PREFIX = "PASSWORD_RESET:PENDING:";
    private static final String USER_INDEX_KEY_PREFIX = "PASSWORD_RESET:USER:";
    private static final long SESSION_EXPIRATION_MINUTES = 5; // 5분 후 만료

    private final UserQueryRepository userQueryRepository;
    private final StringRedisTemplate redisTemplate;

    @Override
    public CreatePasswordResetSessionResponseDto execute(CreatePasswordResetSessionRequestDto request) {
        // 이메일과 전화번호로 사용자 확인
        User user = userQueryRepository.findByEmailAndContact(request.getEmail(), request.getContact())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 기존 세션 확인 및 삭제
        String userIndexKey = USER_INDEX_KEY_PREFIX + user.getId();
        String existingSessionId = redisTemplate.opsForValue().get(userIndexKey);

        if (ObjectUtils.isNotEmpty(existingSessionId)) {
            // 기존 세션 키 삭제
            String existingSessionKey = SESSION_KEY_PREFIX + existingSessionId;
            redisTemplate.delete(existingSessionKey);
            // 기존 인덱스 키 삭제
            redisTemplate.delete(userIndexKey);
        }

        // 새 비밀번호 재설정 세션 생성
        String sessionId = UUID.randomUUID().toString();
        String sessionKey = SESSION_KEY_PREFIX + sessionId;

        // 세션 키 저장: 세션 ID -> 사용자 ID (5분 후 만료)
        redisTemplate.opsForValue().set(
            sessionKey,
            String.valueOf(user.getId()),
            SESSION_EXPIRATION_MINUTES,
            TimeUnit.MINUTES
        );

        // 인덱스 키 저장: 사용자 ID -> 세션 ID (5분 후 만료)
        redisTemplate.opsForValue().set(
            userIndexKey,
            sessionId,
            SESSION_EXPIRATION_MINUTES,
            TimeUnit.MINUTES
        );

        return CreatePasswordResetSessionResponseDto.of(sessionId);
    }
}
