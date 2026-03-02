package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateSignupSessionRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateSignupSessionResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.util.PhoneNumberUtil;
import com.dreamteam.alter.domain.auth.port.outbound.FirebaseTokenVerifier;
import com.dreamteam.alter.domain.user.port.inbound.CreateSignupSessionUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service("createSignupSession")
@RequiredArgsConstructor
@Transactional
public class CreateSignupSession implements CreateSignupSessionUseCase {

    private static final String SESSION_KEY_PREFIX = "SIGNUP:PENDING:";
    private static final String CONTACT_INDEX_KEY_PREFIX = "SIGNUP:CONTACT:";
    private static final long SESSION_EXPIRATION_MINUTES = 10; // 10분 후 만료

    private final UserQueryRepository userQueryRepository;
    private final StringRedisTemplate redisTemplate;
    private final FirebaseTokenVerifier firebaseTokenVerifier;

    @Override
    public CreateSignupSessionResponseDto execute(CreateSignupSessionRequestDto request) {
        // Firebase 토큰 검증 및 전화번호 추출
        String verifiedPhoneNumber = firebaseTokenVerifier.verifyAndGetPhoneNumber(request.getFirebaseIdToken());
        String contact = PhoneNumberUtil.convertE164ToLocal(verifiedPhoneNumber);

        // 휴대폰 번호 중복 확인
        if (userQueryRepository.findByContact(contact).isPresent()) {
            throw new CustomException(ErrorCode.USER_CONTACT_DUPLICATED);
        }

        // 기존 세션 확인 및 삭제
        String contactIndexKey = CONTACT_INDEX_KEY_PREFIX + contact;
        String existingSessionId = redisTemplate.opsForValue().get(contactIndexKey);

        if (ObjectUtils.isNotEmpty(existingSessionId)) {
            String existingSessionKey = SESSION_KEY_PREFIX + existingSessionId;
            redisTemplate.delete(existingSessionKey);
            redisTemplate.delete(contactIndexKey);
        }

        // 회원가입 세션 생성
        String signupSessionId = UUID.randomUUID().toString();
        String sessionKey = SESSION_KEY_PREFIX + signupSessionId;

        // 세션 키 저장: 세션 ID -> 전화번호 (10분 후 만료)
        redisTemplate.opsForValue().set(
            sessionKey,
            contact,
            SESSION_EXPIRATION_MINUTES,
            TimeUnit.MINUTES
        );

        // 인덱스 키 저장: 전화번호 -> 세션 ID (10분 후 만료)
        redisTemplate.opsForValue().set(
            contactIndexKey,
            signupSessionId,
            SESSION_EXPIRATION_MINUTES,
            TimeUnit.MINUTES
        );

        return CreateSignupSessionResponseDto.of(signupSessionId);
    }
}
