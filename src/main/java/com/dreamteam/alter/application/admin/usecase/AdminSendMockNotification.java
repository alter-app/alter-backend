package com.dreamteam.alter.application.admin.usecase;

import com.dreamteam.alter.adapter.inbound.admin.notificaction.dto.AdminSendMockNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.domain.admin.port.inbound.AdminSendMockNotificationUseCase;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminSendMockNotification")
@RequiredArgsConstructor
@Transactional
public class AdminSendMockNotification implements AdminSendMockNotificationUseCase {

    private static final String DEFAULT_MOCK_TITLE = "테스트 알림 제목";
    private static final String DEFAULT_MOCK_BODY = "테스트 알림 내용";

    private final NotificationService notificationService;

    @Override
    public void execute(AdminSendMockNotificationRequestDto request) {
        String title = ObjectUtils.isNotEmpty(request.getTitle()) ? request.getTitle() : DEFAULT_MOCK_TITLE;
        String body = ObjectUtils.isNotEmpty(request.getBody()) ? request.getBody() : DEFAULT_MOCK_BODY;

        notificationService.sendNotification(FcmNotificationRequestDto.of(request.getUserId(), title, body));
    }

}
