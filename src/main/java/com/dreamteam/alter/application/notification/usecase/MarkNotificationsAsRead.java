package com.dreamteam.alter.application.notification.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.notification.command.MarkNotificationsAsReadCommand;
import com.dreamteam.alter.domain.notification.entity.Notification;
import com.dreamteam.alter.domain.notification.port.inbound.MarkNotificationsAsReadUseCase;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("markNotificationsAsRead")
@RequiredArgsConstructor
@Transactional
public class MarkNotificationsAsRead implements MarkNotificationsAsReadUseCase {

    private final NotificationQueryRepository notificationQueryRepository;

    @Override
    public void execute(MarkNotificationsAsReadCommand command) {
        if (command.getNotificationId() != null) {
            Notification notification = notificationQueryRepository.findById(command.getNotificationId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
            if (!notification.getTargetUser().getId().equals(command.getTargetUser().getId())) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
            if (!command.getScope().equals(notification.getScope())) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
            notification.markAsRead();
        } else {
            List<Notification> notifications =
                notificationQueryRepository.findUnreadNotifications(command.getTargetUser(), command.getScope());
            notifications.forEach(Notification::markAsRead);
        }
    }
}
