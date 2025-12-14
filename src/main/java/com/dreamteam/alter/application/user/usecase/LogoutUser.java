package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.port.inbound.LogoutUserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("logoutUser")
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LogoutUser implements LogoutUserUseCase {

    private final AuthService authService;
    private final NotificationService notificationService;

    @Override
    public void execute(AppActor actor) {
        authService.revokeAllExistingAuthorizations(actor.getUser());
        notificationService.removeUserDeviceToken(actor.getUser());
    }
}
