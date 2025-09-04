package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialAuthInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.SocialLoginRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.application.auth.manager.SocialAuthenticationManager;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.ManagerLoginWithSocialUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialQueryRepository;
import com.dreamteam.alter.domain.user.type.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dreamteam.alter.domain.user.entity.UserSocial;

@Service("managerLoginWithSocial")
@RequiredArgsConstructor
@Transactional
public class ManagerLoginWithSocial implements ManagerLoginWithSocialUseCase {

    private final AuthService authService;
    private final UserSocialQueryRepository userSocialQueryRepository;
    private final SocialAuthenticationManager socialAuthenticationManager;

    @Override
    public GenerateTokenResponseDto execute(SocialLoginRequestDto request) {
        SocialAuthInfo socialAuthInfo = socialAuthenticationManager.authenticate(request);

        UserSocial userSocial = userSocialQueryRepository.findBySocialProviderAndSocialId(
            socialAuthInfo.getProvider(),
            socialAuthInfo.getSocialId()
        ).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User user = userSocial.getUser();

        if (!user.getRoles().contains(UserRole.ROLE_MANAGER)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        userSocial.updateRefreshToken(socialAuthInfo.getRefreshToken());

        return GenerateTokenResponseDto.of(authService.generateAuthorization(user, TokenScope.MANAGER));
    }
}
