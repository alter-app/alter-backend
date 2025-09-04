package com.dreamteam.alter.domain.user.port.outbound;

import com.dreamteam.alter.domain.user.entity.UserSocial;
import com.dreamteam.alter.domain.user.type.SocialProvider;

import java.util.Optional;

public interface UserSocialQueryRepository {
    
    Optional<UserSocial> findBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId);
    
    boolean existsBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId);
    
    boolean existsByUserAndSocialProvider(Long userId, SocialProvider socialProvider);
}
