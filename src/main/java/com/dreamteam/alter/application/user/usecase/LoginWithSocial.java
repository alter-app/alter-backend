package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialAuthInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.SocialLoginRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.application.auth.manager.SocialAuthenticationManager;
import com.dreamteam.alter.domain.auth.vo.SocialAuthRequest;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialQueryRepository;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.entity.UserSocial;
import com.dreamteam.alter.domain.user.vo.OauthToken;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.domain.auth.entity.AuthLog;
import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.port.outbound.AuthLogRepository;
import com.dreamteam.alter.domain.auth.type.AuthLogType;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.port.inbound.LoginWithSocialUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("loginWithSocial")
@RequiredArgsConstructor
@Transactional
public class LoginWithSocial implements LoginWithSocialUseCase {

    private final SocialAuthenticationManager socialAuthenticationManager;
    private final UserSocialQueryRepository userSocialQueryRepository;
    private final AuthService authService;
    private final AuthLogRepository authLogRepository;

    @Override
    public GenerateTokenResponseDto execute(SocialLoginRequestDto request) {
        OauthToken oauthToken = request.getOauthToken() != null
            ? OauthToken.of(request.getOauthToken().getAccessToken(), request.getOauthToken().getRefreshToken())
            : null;
        SocialAuthRequest socialAuthRequest = new SocialAuthRequest(
            request.getProvider(),
            oauthToken,
            request.getAuthorizationCode(),
            request.getPlatformType()
        );
        SocialAuthInfo socialAuthInfo = socialAuthenticationManager.authenticate(socialAuthRequest);

        UserSocial userSocial = userSocialQueryRepository.findBySocialProviderAndSocialId(
                socialAuthInfo.getProvider(),
                socialAuthInfo.getSocialId()
            )
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User user = userSocial.getUser();

        switch (user.getStatus()) {
            case SUSPENDED -> throw new CustomException(ErrorCode.SUSPENDED_USER);
            case DELETED -> throw new CustomException(ErrorCode.DELETED_USER);
        }

        userSocial.updateRefreshToken(socialAuthInfo.getRefreshToken());

        // 기존 인가 정보 정리
        authService.revokeAllExistingAuthorizations(user);

        TokenScope scope = switch (user.getRole()) {
            case ROLE_MANAGER -> TokenScope.MANAGER;
            case ROLE_ADMIN -> TokenScope.ADMIN;
            default -> TokenScope.APP;
        };

        Authorization authorization = authService.generateAuthorization(user, scope);
        authLogRepository.save(AuthLog.create(user, authorization, AuthLogType.LOGIN));

        return GenerateTokenResponseDto.of(authorization);
    }
}
