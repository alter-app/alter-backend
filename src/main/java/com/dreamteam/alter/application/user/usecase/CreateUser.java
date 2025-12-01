package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateSignupSessionRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.util.PasswordValidator;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.CreateUserUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.port.outbound.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("createUser")
@RequiredArgsConstructor
@Transactional
public class CreateUser implements CreateUserUseCase {

    private static final String KEY_PREFIX = "SIGNUP:PENDING:";

    private final UserRepository userRepository;
    private final UserQueryRepository userQueryRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public GenerateTokenResponseDto execute(CreateUserRequestDto request) {
        // Redis 세션에서 휴대폰 인증 정보 확인
        String sessionIdKey = KEY_PREFIX + request.getSignupSessionId();
        String userInfoJson = redisTemplate.opsForValue().get(sessionIdKey);

        if (ObjectUtils.isEmpty(userInfoJson)) {
            throw new CustomException(ErrorCode.SIGNUP_SESSION_NOT_EXIST);
        }

        try {
            // Redis에서 사용자 정보 복원
            CreateSignupSessionRequestDto sessionUserInfo = objectMapper.readValue(
                userInfoJson, 
                CreateSignupSessionRequestDto.class
            );

            // 중복 확인 (요청의 email과 세션의 contact 사용)
            validateDuplication(request, sessionUserInfo, sessionIdKey);

            // 비밀번호 형식 검증
            if (!PasswordValidator.isValid(request.getPassword())) {
                throw new CustomException(ErrorCode.INVALID_PASSWORD_FORMAT);
            }

            // 사용자 생성 (요청의 email과 세션의 contact 사용)
            User user = userRepository.save(User.create(
                request.getEmail(),
                sessionUserInfo.getContact(),
                passwordEncoder.encode(request.getPassword()),
                request.getName(),
                request.getNickname(),
                request.getGender(),
                request.getBirthday()
            ));
            
            // 세션 삭제
            redisTemplate.delete(sessionIdKey);

            return GenerateTokenResponseDto.of(authService.generateAuthorization(user, TokenScope.APP));
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 사용자 정보의 중복 여부를 확인합니다.
     */
    private void validateDuplication(CreateUserRequestDto request, CreateSignupSessionRequestDto sessionUserInfo, String sessionIdKey) {
        // 이메일 중복 확인
        if (userQueryRepository.findByEmail(request.getEmail()).isPresent()) {
            redisTemplate.delete(sessionIdKey);
            throw new CustomException(ErrorCode.EMAIL_DUPLICATED);
        }

        // 닉네임 중복 확인
        if (userQueryRepository.findByNickname(request.getNickname()).isPresent()) {
            redisTemplate.delete(sessionIdKey);
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }

        // 연락처 중복 확인
        if (userQueryRepository.findByContact(sessionUserInfo.getContact()).isPresent()) {
            redisTemplate.delete(sessionIdKey);
            throw new CustomException(ErrorCode.USER_CONTACT_DUPLICATED);
        }
    }
}
