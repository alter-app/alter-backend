package com.dreamteam.alter.application.auth.manager;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialAuthInfo;
import com.dreamteam.alter.application.auth.service.AbstractSocialAuth;
import com.dreamteam.alter.domain.auth.vo.SocialAuthRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SocialAuthenticationManager {

    private final List<AbstractSocialAuth> socialAuthServices;

    public SocialAuthInfo authenticate(SocialAuthRequest request) {
        for (AbstractSocialAuth socialAuth : socialAuthServices) {
            if (socialAuth.supports(request.provider())) {
                return socialAuth.authenticate(request);
            }
        }
        throw new IllegalArgumentException("Unsupported provider: " + request.provider());
    }
}
