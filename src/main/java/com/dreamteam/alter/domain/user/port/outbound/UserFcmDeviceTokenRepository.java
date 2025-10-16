package com.dreamteam.alter.domain.user.port.outbound;

import com.dreamteam.alter.domain.user.entity.FCMDeviceToken;

import java.util.List;

public interface UserFcmDeviceTokenRepository {

    void save(FCMDeviceToken fcmDeviceToken);

    void delete(FCMDeviceToken fcmDeviceToken);
    
    void deleteAll(List<FCMDeviceToken> fcmDeviceTokens);
    
}
