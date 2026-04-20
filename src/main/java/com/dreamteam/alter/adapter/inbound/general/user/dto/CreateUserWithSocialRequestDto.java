package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.domain.user.type.PlatformType;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import com.dreamteam.alter.domain.user.type.UserGender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "소셜 회원가입 요청 DTO")
public class CreateUserWithSocialRequestDto {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "회원가입 세션 ID", example = "UUID")
    private String signupSessionId;

    @NotNull
    @Schema(description = "소셜 로그인 플랫폼", example = "KAKAO")
    private SocialProvider provider;

    @Valid
    @Schema(description = "OAuth 토큰")
    private OauthLoginTokenDto oauthToken;

    @Schema(description = "OAuth 인가 코드", example = "authorizationCode")
    private String authorizationCode;

    @NotNull
    @Schema(description = "플랫폼 타입", example = "WEB / NATIVE")
    private PlatformType platformType;

    @NotBlank
    @Size(max = 12)
    @Schema(description = "성명", example = "김철수")
    private String name;

    @NotBlank
    @Size(max = 64)
    @Schema(description = "닉네임", example = "유땡땡")
    private String nickname;

    @NotNull
    @Schema(description = "성별", example = "GENDER_MALE")
    private UserGender gender;

    @NotBlank
    @Size(min = 8, max = 8)
    @Schema(description = "생년월일", example = "YYYYMMDD")
    private String birthday;

    @AssertTrue(message = "WEB 플랫폼은 authorizationCode가 필수입니다")
    private boolean isWebPlatformValid() {
        if (platformType != PlatformType.WEB) return true;
        return authorizationCode != null && !authorizationCode.isBlank();
    }

    @AssertTrue(message = "NATIVE 플랫폼은 oauthToken이 필수입니다")
    private boolean isNativePlatformValid() {
        if (platformType != PlatformType.NATIVE) return true;
        return oauthToken != null;
    }
}
