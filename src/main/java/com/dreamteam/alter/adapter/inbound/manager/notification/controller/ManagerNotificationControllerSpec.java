package com.dreamteam.alter.adapter.inbound.manager.notification.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.MarkNotificationsAsReadRequestDto;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.NotificationListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.NotificationResponseDto;
import com.dreamteam.alter.adapter.inbound.general.notification.dto.UnreadNotificationCountResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "MANAGER - 알림 관련 API")
public interface ManagerNotificationControllerSpec {

    @Operation(summary = "내 알림 목록 조회 (커서 페이징, type/isRead 필터)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공")
    })
    ResponseEntity<CursorPaginatedApiResponse<NotificationResponseDto>> getMyNotifications(
        CursorPageRequestDto pageRequest,
        NotificationListFilterDto filter
    );

    @Operation(summary = "알림 읽음 처리 (notificationId 없으면 전체 읽음 처리)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "읽음 처리 성공")
    })
    ResponseEntity<CommonApiResponse<Void>> markNotificationsAsRead(MarkNotificationsAsReadRequestDto request);

    @Operation(summary = "미읽음 알림 개수 조회 (뱃지용)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "미읽음 알림 개수 조회 성공")
    })
    ResponseEntity<CommonApiResponse<UnreadNotificationCountResponseDto>> getUnreadNotificationCount();
}
