package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateSignupSessionRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateSignupSessionResponseDto;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.port.inbound.CreateSignupSessionUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service("createSignupSession")
@RequiredArgsConstructor
@Transactional
public class CreateSignupSession implements CreateSignupSessionUseCase {

    private static final String KEY_PREFIX = "SIGNUP:PENDING:";
    private static final long SESSION_EXPIRATION_MINUTES = 10; // 10분 후 만료

    private final UserQueryRepository userQueryRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public CreateSignupSessionResponseDto execute(CreateSignupSessionRequestDto request) {
        // 휴대폰 번호 중복 확인
        if (userQueryRepository.findByContact(request.getContact()).isPresent()) {
            throw new CustomException(ErrorCode.USER_CONTACT_DUPLICATED);
        }

        // 회원가입 세션 생성
        String signupSessionId = UUID.randomUUID().toString();
        String sessionKey = KEY_PREFIX + signupSessionId;

        try {
            // 사용자 정보를 JSON으로 직렬화하여 Redis에 저장
            String userInfoJson = objectMapper.writeValueAsString(request);
            
            // 세션 정보를 Redis에 저장 (10분 후 만료)
            redisTemplate.opsForValue().set(
                sessionKey, 
                userInfoJson,
                SESSION_EXPIRATION_MINUTES, 
                TimeUnit.MINUTES
            );

            return CreateSignupSessionResponseDto.of(signupSessionId);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
