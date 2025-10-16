package com.dreamteam.alter.adapter.outbound.user.persistence;

import com.dreamteam.alter.domain.user.entity.FCMDeviceToken;
import com.dreamteam.alter.domain.user.port.outbound.UserFcmDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserFcmDeviceTokenRepositoryImpl implements UserFcmDeviceTokenRepository {
    
    private final UserFCMDeviceTokenJpaRepository userFCMDeviceTokenJpaRepository;
    
    @Override
    public void save(FCMDeviceToken fcmDeviceToken) {
        userFCMDeviceTokenJpaRepository.save(fcmDeviceToken);
    }
    
    @Override
    public void delete(FCMDeviceToken fcmDeviceToken) {
        userFCMDeviceTokenJpaRepository.delete(fcmDeviceToken);
    }
    
    @Override
    public void deleteAll(List<FCMDeviceToken> fcmDeviceTokens) {
        userFCMDeviceTokenJpaRepository.deleteAll(fcmDeviceTokens);
    }
}
