package com.dreamteam.alter.adapter.outbound.auth.external;

import com.dreamteam.alter.domain.auth.entity.SocialRefreshToken;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialRefreshTokenJpaRepository extends JpaRepository<SocialRefreshToken, Long> {

    Optional<SocialRefreshToken> findBySocialProviderAndSocialId(SocialProvider provider, String socialId);

    void deleteBySocialId(String socialId);

}
