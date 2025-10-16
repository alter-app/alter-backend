package com.dreamteam.alter.adapter.outbound.user.persistence;

import com.dreamteam.alter.domain.user.entity.FcmDeviceToken;
import com.dreamteam.alter.domain.user.port.outbound.UserFcmDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserFcmDeviceTokenRepositoryImpl implements UserFcmDeviceTokenRepository {
    
    private final UserFCMDeviceTokenJpaRepository userFCMDeviceTokenJpaRepository;
    
    @Override
    public void save(FcmDeviceToken fcmDeviceToken) {
        userFCMDeviceTokenJpaRepository.save(fcmDeviceToken);
    }
    
    @Override
    public void delete(FcmDeviceToken fcmDeviceToken) {
        userFCMDeviceTokenJpaRepository.delete(fcmDeviceToken);
    }
    
    @Override
    public void deleteAll(List<FcmDeviceToken> fcmDeviceTokens) {
        userFCMDeviceTokenJpaRepository.deleteAll(fcmDeviceTokens);
    }
}
