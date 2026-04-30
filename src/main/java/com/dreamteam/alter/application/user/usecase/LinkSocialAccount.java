package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialAuthInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.OauthLoginTokenDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.SocialLoginRequestDto;
import com.dreamteam.alter.application.auth.manager.SocialAuthenticationManager;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.command.LinkSocialAccountCommand;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.entity.UserSocial;
import com.dreamteam.alter.domain.user.port.inbound.LinkSocialAccountUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialQueryRepository;
import com.dreamteam.alter.domain.user.vo.OauthToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("linkSocialAccount")
@RequiredArgsConstructor
@Transactional
public class LinkSocialAccount implements LinkSocialAccountUseCase {

    private final SocialAuthenticationManager socialAuthenticationManager;
    private final UserSocialQueryRepository userSocialQueryRepository;

    @Override
    public void execute(LinkSocialAccountCommand command) {
        User user = command.getUser();

        OauthToken oauthToken = command.getOauthToken();
        OauthLoginTokenDto oauthLoginToken = oauthToken != null
            ? new OauthLoginTokenDto(oauthToken.getAccessToken(), oauthToken.getRefreshToken())
            : null;
        SocialLoginRequestDto socialAuthRequest = new SocialLoginRequestDto(
            command.getProvider(),
            oauthLoginToken,
            command.getAuthorizationCode(),
            command.getPlatformType()
        );
        SocialAuthInfo socialAuthInfo = socialAuthenticationManager.authenticate(socialAuthRequest);

        if (userSocialQueryRepository.existsByUserAndSocialProvider(user.getId(), socialAuthInfo.getProvider())) {
            throw new CustomException(ErrorCode.SOCIAL_PROVIDER_ALREADY_LINKED);
        }

        if (userSocialQueryRepository.existsBySocialProviderAndSocialId(socialAuthInfo.getProvider(), socialAuthInfo.getSocialId())) {
            throw new CustomException(ErrorCode.SOCIAL_ID_DUPLICATED);
        }

        UserSocial userSocial = UserSocial.create(
            user,
            socialAuthInfo.getProvider(),
            socialAuthInfo.getSocialId(),
            socialAuthInfo.getRefreshToken()
        );

        user.addUserSocial(userSocial);
    }
}
