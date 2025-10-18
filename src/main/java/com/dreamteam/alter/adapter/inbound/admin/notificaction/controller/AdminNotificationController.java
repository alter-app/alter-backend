package com.dreamteam.alter.adapter.inbound.admin.notificaction.controller;

import com.dreamteam.alter.adapter.inbound.admin.notificaction.dto.AdminSendMockNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.domain.admin.port.inbound.AdminSendMockNotificationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/notifications")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminNotificationController implements AdminNotificationControllerSpec {

    private final AdminSendMockNotificationUseCase adminSendMockNotificationUseCase;

    @Override
    @PostMapping("/mock")
    public ResponseEntity<CommonApiResponse<Void>> sendMockNotification(AdminSendMockNotificationRequestDto request) {
        adminSendMockNotificationUseCase.execute(request);

        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
