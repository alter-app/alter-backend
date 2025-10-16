package com.dreamteam.alter.domain.user.port.outbound;

import com.dreamteam.alter.domain.user.entity.FcmDeviceToken;

import java.util.List;

public interface UserFcmDeviceTokenRepository {

    void save(FcmDeviceToken fcmDeviceToken);

    void delete(FcmDeviceToken fcmDeviceToken);
    
    void deleteAll(List<FcmDeviceToken> fcmDeviceTokens);
    
}
