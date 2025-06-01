package com.dreamteam.alter.adapter.outbound.auth.external;

import com.dreamteam.alter.domain.auth.entity.SocialRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialRefreshTokenJpaRepository extends JpaRepository<SocialRefreshToken, Long> {

    Optional<SocialRefreshToken> findBySocialId(String socialId);

    void deleteBySocialId(String socialId);

}
