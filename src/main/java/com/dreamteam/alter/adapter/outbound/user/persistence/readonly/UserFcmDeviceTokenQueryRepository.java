package com.dreamteam.alter.adapter.outbound.user.persistence.readonly;

import com.dreamteam.alter.domain.user.entity.FcmDeviceToken;
import com.dreamteam.alter.domain.user.entity.User;

import java.util.Optional;

public interface UserFcmDeviceTokenQueryRepository {
    Optional<FcmDeviceToken> findByUser(User user);
    Optional<FcmDeviceToken> findByDeviceToken(String deviceToken);
}
