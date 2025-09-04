package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialAuthInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.LinkSocialAccountRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.SocialLoginRequestDto;
import com.dreamteam.alter.application.auth.manager.SocialAuthenticationManager;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.entity.UserSocial;
import com.dreamteam.alter.domain.user.port.inbound.LinkSocialAccountUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialQueryRepository;
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
    public void execute(AppActor actor, LinkSocialAccountRequestDto request) {
        User user = actor.getUser();
        
        // 소셜 인증 처리
        SocialLoginRequestDto socialAuthRequest = new SocialLoginRequestDto(
            request.getProvider(),
            request.getOauthToken(),
            request.getAuthorizationCode(),
            request.getPlatformType()
        );
        SocialAuthInfo socialAuthInfo = socialAuthenticationManager.authenticate(socialAuthRequest);

        // 해당 provider에 대한 계정이 이미 연동되어 있는지 확인
        if (userSocialQueryRepository.existsByUserAndSocialProvider(user.getId(), socialAuthInfo.getProvider())) {
            throw new CustomException(ErrorCode.SOCIAL_PROVIDER_ALREADY_LINKED);
        }

        // 이미 연동된 소셜 계정인지 확인 (다른 사용자가 사용 중인지)
        if (userSocialQueryRepository.existsBySocialProviderAndSocialId(socialAuthInfo.getProvider(), socialAuthInfo.getSocialId())) {
            throw new CustomException(ErrorCode.SOCIAL_ID_DUPLICATED);
        }

        // 소셜 계정 연동 정보 생성 및 연관관계 설정
        UserSocial userSocial = UserSocial.create(
            user,
            socialAuthInfo.getProvider(),
            socialAuthInfo.getSocialId(),
            socialAuthInfo.getRefreshToken()
        );

        user.addUserSocial(userSocial);
    }
}
