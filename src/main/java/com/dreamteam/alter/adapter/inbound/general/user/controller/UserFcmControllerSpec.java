package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.RegisterFcmDeviceTokenRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "APP - 사용자 FCM")
public interface UserFcmControllerSpec {

    // TODO: 응답 스펙 예시 수정 필요
    @Operation(summary = "FCM 디바이스 토큰 등록",
        description = "사용자의 FCM 디바이스 토큰을 등록하거나 갱신합니다.")
    ResponseEntity<CommonApiResponse<Void>> registerDeviceToken(
        @Valid @RequestBody RegisterFcmDeviceTokenRequestDto request
    );
}
