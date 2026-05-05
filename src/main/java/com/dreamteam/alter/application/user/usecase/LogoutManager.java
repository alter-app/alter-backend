package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.port.inbound.LogoutManagerUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("logoutManager")
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LogoutManager implements LogoutManagerUseCase {

    private final AuthService authService;
    private final NotificationService notificationService;

    @Override
    public void execute(ManagerActor actor) {
        authService.revokeAllExistingAuthorizations(actor.getManagerUser().getUser());
        notificationService.removeUserDeviceToken(actor.getManagerUser().getUser());
    }
}
