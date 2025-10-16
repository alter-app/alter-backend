package com.dreamteam.alter.adapter.outbound.user.persistence.readonly;

import com.dreamteam.alter.domain.user.entity.FCMDeviceToken;
import com.dreamteam.alter.domain.user.entity.User;

import java.util.Optional;

public interface UserFcmDeviceTokenQueryRepository {
    Optional<FCMDeviceToken> findByUser(User user);
    Optional<FCMDeviceToken> findByDeviceToken(String deviceToken);
}
