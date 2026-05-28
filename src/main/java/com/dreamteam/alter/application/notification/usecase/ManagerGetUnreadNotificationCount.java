package com.dreamteam.alter.application.notification.usecase;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.port.inbound.ManagerGetUnreadNotificationCountUseCase;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationQueryRepository;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("managerGetUnreadNotificationCount")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerGetUnreadNotificationCount implements ManagerGetUnreadNotificationCountUseCase {

    private final NotificationQueryRepository notificationQueryRepository;

    @Override
    public long execute(ManagerActor actor) {
        User targetUser = actor.getManagerUser().getUser();
        return notificationQueryRepository.getCountOfUnreadNotifications(targetUser, TokenScope.MANAGER);
    }
}
