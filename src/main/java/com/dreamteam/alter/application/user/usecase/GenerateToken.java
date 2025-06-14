package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SignupSessionResponseDto;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialUserInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.LoginUserRequestDto;
import com.dreamteam.alter.application.auth.manager.SocialAuthenticationManager;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.exception.SignupRequiredException;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.GenerateTokenUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.type.UserRole;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service("generateToken")
@RequiredArgsConstructor
public class GenerateToken implements GenerateTokenUseCase {

    private final AuthService authService;
    private final StringRedisTemplate redisTemplate;
    private final UserQueryRepository userQueryRepository;
    private final SocialAuthenticationManager socialAuthenticationManager;

    @Override
    public GenerateTokenResponseDto execute(LoginUserRequestDto request) {
        SocialUserInfo socialUserInfo = authenticateSocialUser(request);

        User user = userQueryRepository.findBySocialId(socialUserInfo.getSocialId());

        if (ObjectUtils.isEmpty(user)) {
            try {
                if (ObjectUtils.isNotEmpty(userQueryRepository.findByEmail(socialUserInfo.getEmail()))) {
                    throw new CustomException(ErrorCode.EMAIL_DUPLICATED);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonValue = objectMapper.writeValueAsString(socialUserInfo);

                String signupSessionId = UUID.randomUUID().toString();

                String key = "SIGNUP:PENDING:" + signupSessionId;
                redisTemplate.opsForValue().set(key, jsonValue, 5, TimeUnit.MINUTES);

                throw new SignupRequiredException(SignupSessionResponseDto.of(signupSessionId, socialUserInfo));
            } catch (JsonProcessingException e) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }

        if (!user.getRoles().contains(UserRole.ROLE_USER)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return GenerateTokenResponseDto.of(authService.generateAuthorization(user, TokenScope.APP));
    }

    private SocialUserInfo authenticateSocialUser(LoginUserRequestDto request) {
        return socialAuthenticationManager.authenticate(request);
    }

}
