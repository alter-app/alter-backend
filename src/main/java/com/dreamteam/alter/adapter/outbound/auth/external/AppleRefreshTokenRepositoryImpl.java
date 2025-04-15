package com.dreamteam.alter.adapter.outbound.auth.external;

import com.dreamteam.alter.domain.auth.entity.AppleRefreshToken;
import com.dreamteam.alter.domain.auth.port.outbound.AppleRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional
public class AppleRefreshTokenRepositoryImpl implements AppleRefreshTokenRepository {

    private final AppleRefreshTokenJpaRepository appleRefreshTokenJpaRepository;

    @Override
    public void saveOrUpdate(String socialId, String refreshToken) {
        appleRefreshTokenJpaRepository.findBySocialId(socialId)
            .ifPresentOrElse(
                appleRefreshToken -> appleRefreshToken.update(refreshToken),
                () -> appleRefreshTokenJpaRepository.save(AppleRefreshToken.create(socialId, refreshToken))
            );
    }

    @Override
    public void deleteBySocialId(String socialId) {
        appleRefreshTokenJpaRepository.deleteBySocialId(socialId);
    }

}
