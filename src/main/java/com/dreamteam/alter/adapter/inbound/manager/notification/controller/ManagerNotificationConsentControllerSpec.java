package com.dreamteam.alter.adapter.inbound.manager.notification.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.NotificationConsentResponseDto;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.UpdateNotificationConsentRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "MANAGER - 알림 수신 설정 API")
public interface ManagerNotificationConsentControllerSpec {

    @Operation(summary = "알림 수신 설정 조회")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "알림 수신 설정 조회 성공")
    })
    ResponseEntity<CommonApiResponse<NotificationConsentResponseDto>> getNotificationConsent();

    @Operation(summary = "알림 수신 설정 변경")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "알림 수신 설정 변경 성공")
    })
    ResponseEntity<CommonApiResponse<Void>> updateNotificationConsent(
        @Valid @RequestBody UpdateNotificationConsentRequestDto request
    );
}
