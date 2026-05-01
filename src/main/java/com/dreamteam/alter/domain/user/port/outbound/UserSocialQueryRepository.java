package com.dreamteam.alter.domain.user.port.outbound;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.entity.UserSocial;
import com.dreamteam.alter.domain.user.type.SocialProvider;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

public interface UserSocialQueryRepository {

    Optional<UserSocial> findBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId);

    Optional<UserSocial> findByUserIdAndSocialProvider(Long userId, SocialProvider socialProvider);

    Map<SocialProvider, LocalDateTime> findLinkedSocialAccountsByUserId(User user);

    boolean existsBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId);

    boolean existsByUserAndSocialProvider(Long userId, SocialProvider socialProvider);

    long countByUserIdForUpdate(Long userId);
}
