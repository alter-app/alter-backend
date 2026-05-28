package com.dreamteam.alter.application.notification.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.entity.Notification;
import com.dreamteam.alter.domain.notification.port.inbound.MarkNotificationsAsReadUseCase;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationRepository;
import com.dreamteam.alter.domain.user.context.AppActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("markNotificationsAsRead")
@RequiredArgsConstructor
@Transactional
public class MarkNotificationsAsRead implements MarkNotificationsAsReadUseCase {

    private final NotificationRepository notificationRepository;

    @Override
    public void execute(AppActor actor, Long notificationId) {
        if (notificationId != null) {
            Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
            if (!notification.getTargetUser().getId().equals(actor.getUser().getId())) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
            notification.markAsRead();
        } else {
            notificationRepository.markAllAsRead(actor.getUser(), TokenScope.APP);
        }
    }
}
