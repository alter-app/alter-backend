package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialUserInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.LoginUserRequestDto;
import com.dreamteam.alter.application.auth.manager.SocialAuthenticationManager;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.GenerateTokenUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.type.UserRole;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service("managerGenerateToken")
@RequiredArgsConstructor
public class ManagerGenerateToken implements GenerateTokenUseCase {

    private final AuthService authService;
    private final UserQueryRepository userQueryRepository;
    private final SocialAuthenticationManager socialAuthenticationManager;

    @Override
    public GenerateTokenResponseDto execute(LoginUserRequestDto request) {
        SocialUserInfo socialUserInfo = authenticateSocialUser(request);

        User user = userQueryRepository.findBySocialId(socialUserInfo.getSocialId());

        if (ObjectUtils.isEmpty(user)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        if (!user.getRoles().contains(UserRole.ROLE_MANAGER)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return GenerateTokenResponseDto.of(authService.generateAuthorization(user, TokenScope.MANAGER));
    }

    private SocialUserInfo authenticateSocialUser(LoginUserRequestDto request) {
        return socialAuthenticationManager.authenticate(request);
    }

}
