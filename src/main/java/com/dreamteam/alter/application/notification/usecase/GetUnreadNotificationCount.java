package com.dreamteam.alter.application.notification.usecase;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.port.inbound.GetUnreadNotificationCountUseCase;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationQueryRepository;
import com.dreamteam.alter.domain.user.context.AppActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("getUnreadNotificationCount")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUnreadNotificationCount implements GetUnreadNotificationCountUseCase {

    private final NotificationQueryRepository notificationQueryRepository;

    @Override
    public long execute(AppActor actor) {
        return notificationQueryRepository.getCountOfUnreadNotifications(actor.getUser(), TokenScope.APP);
    }
}
