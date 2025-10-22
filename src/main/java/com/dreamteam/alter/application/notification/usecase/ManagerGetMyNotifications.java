package com.dreamteam.alter.application.notification.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.NotificationResponseDto;
import com.dreamteam.alter.adapter.outbound.notification.persistence.readonly.NotificationResponse;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.notification.port.inbound.ManagerGetMyNotificationsUseCase;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("managerGetMyNotifications")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerGetMyNotifications implements ManagerGetMyNotificationsUseCase {

    private final NotificationQueryRepository notificationQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<NotificationResponseDto> execute(ManagerActor actor, CursorPageRequestDto pageRequest) {
        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(pageRequest.cursor())) {
            cursorDto = CursorUtil.decodeCursor(pageRequest.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> cursorPageRequest = CursorPageRequest.of(cursorDto, pageRequest.pageSize());

        // ManagerActor에서 User 추출
        User targetUser = actor.getManagerUser().getUser();

        long count = notificationQueryRepository.getCountOfNotifications(targetUser);
        if (count == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequest.pageSize(), (int) count));
        }

        List<NotificationResponse> notifications = notificationQueryRepository.getNotificationsWithCursor(
            cursorPageRequest, targetUser
        );
        if (ObjectUtils.isEmpty(notifications)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequest.pageSize(), (int) count));
        }

        NotificationResponse last = notifications.getLast();
        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.id(), last.createdAt()), objectMapper),
            pageRequest.pageSize(),
            (int) count
        );

        return CursorPaginatedApiResponse.of(
            pageResponseDto,
            notifications.stream()
                .map(NotificationResponseDto::from)
                .toList()
        );
    }
}
