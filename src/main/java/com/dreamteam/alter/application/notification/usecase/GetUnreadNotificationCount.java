package com.dreamteam.alter.application.notification.usecase;

import com.dreamteam.alter.adapter.inbound.general.notification.dto.UnreadNotificationCountResponseDto;
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
    public UnreadNotificationCountResponseDto execute(AppActor actor) {
        long count = notificationQueryRepository.getCountOfUnreadNotifications(actor.getUser(), TokenScope.APP);
        return UnreadNotificationCountResponseDto.of(count);
    }
}
