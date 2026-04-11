package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialAuthInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserWithSocialRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.SocialLoginRequestDto;
import com.dreamteam.alter.application.auth.manager.SocialAuthenticationManager;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.entity.AuthLog;
import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.port.outbound.AuthLogRepository;
import com.dreamteam.alter.domain.auth.type.AuthLogType;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.entity.UserSocial;
import com.dreamteam.alter.domain.user.port.inbound.CreateUserWithSocialUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.port.outbound.UserRepository;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("createUserWithSocial")
@RequiredArgsConstructor
@Transactional
public class CreateUserWithSocial implements CreateUserWithSocialUseCase {

    private static final String KEY_PREFIX = "SIGNUP:PENDING:";
    private static final String CONTACT_INDEX_KEY_PREFIX = "SIGNUP:CONTACT:";

    private final UserRepository userRepository;
    private final UserQueryRepository userQueryRepository;
    private final UserSocialQueryRepository userSocialQueryRepository;
    private final SocialAuthenticationManager socialAuthenticationManager;
    private final AuthService authService;
    private final AuthLogRepository authLogRepository;
    private final StringRedisTemplate redisTemplate;

    @Override
    public GenerateTokenResponseDto execute(CreateUserWithSocialRequestDto request) {

        // Redis 세션에서 휴대폰 인증 정보 확인
        String sessionIdKey = KEY_PREFIX + request.getSignupSessionId();
        String contact = redisTemplate.opsForValue().get(sessionIdKey);

        if (ObjectUtils.isEmpty(contact)) {
            throw new CustomException(ErrorCode.SIGNUP_SESSION_NOT_EXIST);
        }

        // 중복 확인
        validateDuplication(request, contact, sessionIdKey);

        // 소셜 인증
        SocialLoginRequestDto socialAuthRequest = new SocialLoginRequestDto(
            request.getProvider(),
            request.getOauthToken(),
            request.getAuthorizationCode(),
            request.getPlatformType()
        );
        SocialAuthInfo socialAuthInfo = socialAuthenticationManager.authenticate(socialAuthRequest);

        // 이미 연동된 소셜 계정인지 확인
        if (userSocialQueryRepository.existsBySocialProviderAndSocialId(
            socialAuthInfo.getProvider(), socialAuthInfo.getSocialId()
        )) {
            throw new CustomException(ErrorCode.SOCIAL_ID_DUPLICATED);
        }

        // 소셜 계정 이메일 중복 확인
        String email = socialAuthInfo.getEmail();
        if (ObjectUtils.isNotEmpty(email)) {
            userQueryRepository.findByEmail(email)
                .ifPresent(existing -> { throw new CustomException(ErrorCode.EMAIL_DUPLICATED); });
        }

        // 사용자 생성
        User user = userRepository.save(User.createWithSocial(
            contact,
            request.getName(),
            request.getNickname(),
            request.getGender(),
            request.getBirthday(),
            email
        ));

        // 소셜 계정 연동
        UserSocial userSocial = UserSocial.create(
            user,
            socialAuthInfo.getProvider(),
            socialAuthInfo.getSocialId(),
            socialAuthInfo.getRefreshToken()
        );
        user.addUserSocial(userSocial);

        // 회원가입 세션 삭제
        redisTemplate.delete(sessionIdKey);
        redisTemplate.delete(CONTACT_INDEX_KEY_PREFIX + contact);

        Authorization authorization = authService.generateAuthorization(user, TokenScope.APP);
        authLogRepository.save(AuthLog.create(user, authorization, AuthLogType.LOGIN));

        return GenerateTokenResponseDto.of(authorization);
    }

    private void validateDuplication(CreateUserWithSocialRequestDto request, String contact, String sessionIdKey) {
        // 닉네임 중복 확인
        if (userQueryRepository.findByNickname(request.getNickname()).isPresent()) {
            redisTemplate.delete(sessionIdKey);
            redisTemplate.delete(CONTACT_INDEX_KEY_PREFIX + contact);
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }

        // 연락처 중복 확인
        if (userQueryRepository.findByContact(contact).isPresent()) {
            redisTemplate.delete(sessionIdKey);
            redisTemplate.delete(CONTACT_INDEX_KEY_PREFIX + contact);
            throw new CustomException(ErrorCode.USER_CONTACT_DUPLICATED);
        }
    }
}
