package com.dreamteam.alter.domain.auth.port.outbound;

public interface SocialRefreshTokenRepository {
    void saveOrUpdate(String socialId, String refreshToken);
    void deleteBySocialId(String socialId);
}
