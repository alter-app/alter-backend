package com.dreamteam.alter.adapter.inbound.admin.notification.controller;

import com.dreamteam.alter.adapter.inbound.admin.notification.dto.AdminSendMockNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "ADMIN - 관리자 알림 API")
public interface AdminNotificationControllerSpec {

    @Operation(summary = "테스트용 알림 발송", description = "특정 사용자에게 테스트용 푸시 알림을 발송합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "알림 발송 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "잘못된 요청 (필수 값 누락)",
                        value = "{\"code\" : \"B001\"}"
                    )
                })),
        @ApiResponse(responseCode = "404", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 사용자",
                        value = "{\"code\" : \"B011\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> sendMockNotification(
        @Valid @RequestBody AdminSendMockNotificationRequestDto request
    );
}
