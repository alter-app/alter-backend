package com.dreamteam.alter.domain.auth.port.outbound;

public interface AppleRefreshTokenRepository {
    void saveOrUpdate(String socialId, String refreshToken);
    void deleteBySocialId(String socialId);
}
