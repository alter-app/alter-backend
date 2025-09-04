package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialAuthInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.SocialLoginRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.application.auth.manager.SocialAuthenticationManager;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialQueryRepository;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.entity.UserSocial;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.application.auth.service.AuthService;
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

    @Override
    public GenerateTokenResponseDto execute(SocialLoginRequestDto request) {
        SocialAuthInfo socialAuthInfo = socialAuthenticationManager.authenticate(request);

        UserSocial userSocial = userSocialQueryRepository.findBySocialProviderAndSocialId(
                socialAuthInfo.getProvider(),
                socialAuthInfo.getSocialId()
            )
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User user = userSocial.getUser();

        userSocial.updateRefreshToken(socialAuthInfo.getRefreshToken());

        return GenerateTokenResponseDto.of(authService.generateAuthorization(user, TokenScope.APP));
    }
}
