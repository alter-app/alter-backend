package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.RegisterFcmDeviceTokenRequestDto;
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

@Tag(name = "APP - 사용자 FCM")
public interface UserFcmControllerSpec {

    @Operation(summary = "FCM 디바이스 토큰 등록",
        description = "사용자의 FCM 디바이스 토큰을 등록하거나 갱신합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "FCM 디바이스 토큰 등록 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "잘못된 요청 (deviceToken 또는 devicePlatform 누락)",
                        value = "{\"code\" : \"B001\"}"
                    ),
                    @ExampleObject(
                        name = "존재하지 않는 사용자",
                        value = "{\"code\" : \"B011\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> registerDeviceToken(
        @Valid @RequestBody RegisterFcmDeviceTokenRequestDto request
    );
}
