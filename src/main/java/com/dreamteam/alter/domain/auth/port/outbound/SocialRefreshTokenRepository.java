package com.dreamteam.alter.domain.auth.port.outbound;

import com.dreamteam.alter.domain.user.type.SocialProvider;

public interface SocialRefreshTokenRepository {
    void saveOrUpdate(SocialProvider provider, String socialId, String refreshToken);
    void deleteBySocialId(String socialId);
}
