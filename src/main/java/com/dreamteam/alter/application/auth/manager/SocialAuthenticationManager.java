package com.dreamteam.alter.application.auth.manager;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialUserInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.SocialLoginRequestDto;
import com.dreamteam.alter.application.auth.service.AbstractSocialAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SocialAuthenticationManager {

    private final List<AbstractSocialAuth> socialAuthServices;

    public SocialUserInfo authenticate(SocialLoginRequestDto request) {
        for (AbstractSocialAuth socialAuth : socialAuthServices) {
            if (socialAuth.supports(request.getProvider())) {
                return socialAuth.authenticate(request);
            }
        }
        throw new IllegalArgumentException("Unsupported provider: " + request.getProvider());
    }
}
