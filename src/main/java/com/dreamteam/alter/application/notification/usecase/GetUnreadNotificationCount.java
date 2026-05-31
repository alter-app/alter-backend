package com.dreamteam.alter.application.notification.usecase;

import com.dreamteam.alter.domain.notification.command.GetUnreadNotificationCountCommand;
import com.dreamteam.alter.domain.notification.port.inbound.GetUnreadNotificationCountUseCase;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("getUnreadNotificationCount")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUnreadNotificationCount implements GetUnreadNotificationCountUseCase {

    private final NotificationQueryRepository notificationQueryRepository;

    @Override
    public long execute(GetUnreadNotificationCountCommand command) {
        return notificationQueryRepository.getCountOfUnreadNotifications(command.getTargetUser(), command.getScope());
    }
}
