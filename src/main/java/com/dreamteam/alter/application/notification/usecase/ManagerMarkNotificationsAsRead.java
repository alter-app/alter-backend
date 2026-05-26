package com.dreamteam.alter.application.notification.usecase;

import com.dreamteam.alter.adapter.inbound.general.notification.dto.MarkNotificationsAsReadRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.entity.Notification;
import com.dreamteam.alter.domain.notification.port.inbound.ManagerMarkNotificationsAsReadUseCase;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("managerMarkNotificationsAsRead")
@RequiredArgsConstructor
@Transactional
public class ManagerMarkNotificationsAsRead implements ManagerMarkNotificationsAsReadUseCase {

    private final NotificationRepository notificationRepository;

    @Override
    public void execute(ManagerActor actor, MarkNotificationsAsReadRequestDto request) {
        User targetUser = actor.getManagerUser().getUser();

        if (request.getNotificationId() != null) {
            Notification notification = notificationRepository.findById(request.getNotificationId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
            if (!notification.getTargetUser().getId().equals(targetUser.getId())) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
            notification.markAsRead();
        } else {
            notificationRepository.markAllAsRead(targetUser, TokenScope.MANAGER);
        }
    }
}
