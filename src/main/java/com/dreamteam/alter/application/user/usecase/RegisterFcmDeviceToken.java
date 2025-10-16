package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.RegisterFcmDeviceTokenRequestDto;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.port.inbound.RegisterFcmDeviceTokenUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("registerFCMDeviceToken")
@RequiredArgsConstructor
@Transactional
public class RegisterFcmDeviceToken implements RegisterFcmDeviceTokenUseCase {

    private final NotificationService notificationService;

    @Override
    public void execute(AppActor actor, RegisterFcmDeviceTokenRequestDto request) {
        notificationService.saveOrUpdateUserDeviceToken(
            actor.getUser(),
            request.getDeviceToken(),
            request.getDevicePlatform()
        );
    }

}
