package com.dreamteam.alter.adapter.outbound.auth.external;

import com.dreamteam.alter.domain.auth.entity.AppleRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppleRefreshTokenJpaRepository extends JpaRepository<AppleRefreshToken, Long> {

    Optional<AppleRefreshToken> findBySocialId(String socialId);

    void deleteBySocialId(String socialId);

}
