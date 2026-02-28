package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.util.PasswordValidator;
import com.dreamteam.alter.domain.auth.entity.AuthLog;
import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.port.outbound.AuthLogRepository;
import com.dreamteam.alter.domain.auth.type.AuthLogType;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.CreateUserUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.port.outbound.UserRepository;
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
    private static final String CONTACT_INDEX_KEY_PREFIX = "SIGNUP:CONTACT:";

    private final UserRepository userRepository;
    private final UserQueryRepository userQueryRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final AuthLogRepository authLogRepository;

    @Override
    public GenerateTokenResponseDto execute(CreateUserRequestDto request) {

        // Redis 세션에서 휴대폰 인증 정보 확인
        String sessionIdKey = KEY_PREFIX + request.getSignupSessionId();
        String contact = redisTemplate.opsForValue().get(sessionIdKey);

        if (ObjectUtils.isEmpty(contact)) {
            throw new CustomException(ErrorCode.SIGNUP_SESSION_NOT_EXIST);
        }

        // 중복 확인
        validateDuplication(request, contact, sessionIdKey);

        // 비밀번호 형식 검증
        if (!PasswordValidator.isValid(request.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }

        // 사용자 생성
        User user = userRepository.save(User.create(
            contact,
            passwordEncoder.encode(request.getPassword()),
            request.getName(),
            request.getNickname(),
            request.getGender(),
            request.getBirthday()
        ));

        // 회원가입 세션 삭제
        redisTemplate.delete(sessionIdKey);
        redisTemplate.delete(CONTACT_INDEX_KEY_PREFIX + contact);

        Authorization authorization = authService.generateAuthorization(user, TokenScope.APP);
        authLogRepository.save(AuthLog.create(user, authorization, AuthLogType.LOGIN));

        return GenerateTokenResponseDto.of(authorization);
    }

    private void validateDuplication(CreateUserRequestDto request, String contact, String sessionIdKey) {
        // 닉네임 중복 확인
        if (userQueryRepository.findByNickname(request.getNickname()).isPresent()) {
            redisTemplate.delete(sessionIdKey);
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }

        // 연락처 중복 확인
        if (userQueryRepository.findByContact(contact).isPresent()) {
            redisTemplate.delete(sessionIdKey);
            throw new CustomException(ErrorCode.USER_CONTACT_DUPLICATED);
        }
    }
}
