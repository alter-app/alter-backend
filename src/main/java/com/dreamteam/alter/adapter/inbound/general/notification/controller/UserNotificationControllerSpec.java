package com.dreamteam.alter.adapter.inbound.general.notification.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.NotificationResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "APP - 알림 관련 API")
public interface UserNotificationControllerSpec {

    @Operation(summary = "내 알림 목록 조회 (커서 페이징)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공")
    })
    ResponseEntity<CursorPaginatedApiResponse<NotificationResponseDto>> getMyNotifications(
        CursorPageRequestDto pageRequest
    );
}
