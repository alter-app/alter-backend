package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialUserInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
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
import org.springframework.stereotype.Service;

@Service("createUser")
@RequiredArgsConstructor
public class CreateUser implements CreateUserUseCase {

    private static final String KEY_PREFIX = "SIGNUP:PENDING:";

    private final UserRepository userRepository;
    private final UserQueryRepository userQueryRepository;
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public GenerateTokenResponseDto execute(CreateUserRequestDto request) {
        String sessionIdKey = KEY_PREFIX + request.getSignupSessionId();
        String userInfoJson = redisTemplate.opsForValue()
            .get(sessionIdKey);

        if (ObjectUtils.isEmpty(userInfoJson)) {
            throw new CustomException(ErrorCode.SIGNUP_SESSION_NOT_EXIST);
        }

        try {
            SocialUserInfo socialUserInfo = objectMapper.readValue(userInfoJson, SocialUserInfo.class);

            validateDuplication(socialUserInfo, request);

            User user = userRepository.save(User.create(
                request,
                socialUserInfo
            ));
            redisTemplate.delete(sessionIdKey);

            return GenerateTokenResponseDto.of(authService.generateAuthorization(user, TokenScope.APP));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateDuplication(SocialUserInfo socialUserInfo, CreateUserRequestDto request) {
        checkDuplication(userQueryRepository.findBySocialId(socialUserInfo.getSocialId()), ErrorCode.SOCIAL_ID_DUPLICATED);
        checkDuplication(userQueryRepository.findByEmail(socialUserInfo.getEmail()), ErrorCode.EMAIL_DUPLICATED);
        checkDuplication(userQueryRepository.findByNickname(request.getNickname()), ErrorCode.NICKNAME_DUPLICATED);
    }

    private void checkDuplication(User user, ErrorCode errorCode) {
        if (ObjectUtils.isNotEmpty(user)) {
            throw new CustomException(errorCode);
        }
    }

}
