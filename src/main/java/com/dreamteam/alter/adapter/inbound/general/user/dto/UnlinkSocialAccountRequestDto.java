package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.domain.user.type.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "소셜 계정 연동 해제 요청 DTO")
public class UnlinkSocialAccountRequestDto {

    @NotNull
    @Schema(description = "해제할 소셜 플랫폼", example = "KAKAO")
    private SocialProvider provider;
}
