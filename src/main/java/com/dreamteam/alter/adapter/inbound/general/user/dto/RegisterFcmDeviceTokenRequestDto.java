package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.domain.user.type.DevicePlatformType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "FCM 디바이스 토큰 등록 요청 DTO")
public class RegisterFcmDeviceTokenRequestDto {
    
    @NotBlank
    @Size(max = 500)
    @Schema(description = "FCM 디바이스 토큰")
    private String deviceToken;
    
    @NotNull
    @Schema(description = "디바이스 플랫폼")
    private DevicePlatformType devicePlatform;
}
