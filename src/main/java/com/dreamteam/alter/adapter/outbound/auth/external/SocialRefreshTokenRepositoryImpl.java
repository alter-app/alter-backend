package com.dreamteam.alter.adapter.outbound.auth.external;

import com.dreamteam.alter.domain.auth.entity.SocialRefreshToken;
import com.dreamteam.alter.domain.auth.port.outbound.SocialRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional
public class SocialRefreshTokenRepositoryImpl implements SocialRefreshTokenRepository {

    private final SocialRefreshTokenJpaRepository socialRefreshTokenJpaRepository;

    @Override
    public void saveOrUpdate(String socialId, String refreshToken) {
        socialRefreshTokenJpaRepository.findBySocialId(socialId)
            .ifPresentOrElse(
                socialRefreshToken -> socialRefreshToken.update(refreshToken),
                () -> socialRefreshTokenJpaRepository.save(SocialRefreshToken.create(socialId, refreshToken))
            );
    }

    @Override
    public void deleteBySocialId(String socialId) {
        socialRefreshTokenJpaRepository.deleteBySocialId(socialId);
    }

}
