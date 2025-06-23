package com.dreamteam.alter.adapter.outbound.auth.external;

import com.dreamteam.alter.domain.auth.entity.SocialRefreshToken;
import com.dreamteam.alter.domain.auth.port.outbound.SocialRefreshTokenRepository;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional
public class SocialRefreshTokenRepositoryImpl implements SocialRefreshTokenRepository {

    private final SocialRefreshTokenJpaRepository socialRefreshTokenJpaRepository;

    @Override
    public void saveOrUpdate(SocialProvider provider, String socialId, String refreshToken) {
        socialRefreshTokenJpaRepository.findBySocialProviderAndSocialId(provider, socialId)
            .ifPresentOrElse(
                socialRefreshToken -> socialRefreshToken.update(refreshToken),
                () -> socialRefreshTokenJpaRepository.save(SocialRefreshToken.create(provider, socialId, refreshToken))
            );
    }

    @Override
    public void deleteBySocialId(String socialId) {
        socialRefreshTokenJpaRepository.deleteBySocialId(socialId);
    }

}
