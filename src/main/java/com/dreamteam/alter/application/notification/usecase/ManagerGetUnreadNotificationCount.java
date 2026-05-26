package com.dreamteam.alter.application.notification.usecase;

import com.dreamteam.alter.adapter.inbound.general.notification.dto.UnreadNotificationCountResponseDto;
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
    public UnreadNotificationCountResponseDto execute(ManagerActor actor) {
        User targetUser = actor.getManagerUser().getUser();
        long count = notificationQueryRepository.getCountOfUnreadNotifications(targetUser, TokenScope.MANAGER);
        return UnreadNotificationCountResponseDto.of(count);
    }
}
