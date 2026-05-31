package com.dreamteam.alter.adapter.inbound.general.notification.mapper;

import com.dreamteam.alter.adapter.inbound.general.notification.dto.MarkNotificationsAsReadRequestDto;
import com.dreamteam.alter.domain.notification.command.MarkNotificationsAsReadCommand;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MarkNotificationsAsReadCommandMapper {

    public static MarkNotificationsAsReadCommand toCommand(AppActor actor, MarkNotificationsAsReadRequestDto request) {
        Long notificationId = ObjectUtils.isNotEmpty(request) ? request.getNotificationId() : null;
        return MarkNotificationsAsReadCommand.of(actor, notificationId);
    }

    public static MarkNotificationsAsReadCommand toCommand(ManagerActor actor, MarkNotificationsAsReadRequestDto request) {
        Long notificationId = ObjectUtils.isNotEmpty(request) ? request.getNotificationId() : null;
        return MarkNotificationsAsReadCommand.of(actor, notificationId);
    }
}
