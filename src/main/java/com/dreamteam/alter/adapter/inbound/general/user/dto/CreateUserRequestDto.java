package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.domain.user.type.UserGender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 요청 DTO")
public class CreateUserRequestDto {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "회원가입 세션 ID", example = "UUID")
    private String signupSessionId;

    @Nullable
    @Size(max = 64)
    @Schema(description = "이메일 인증 세션 ID (선택)")
    private String emailSessionId;

    @NotBlank
    @Size(min = 8, max = 16)
    @Schema(description = "비밀번호", example = "password123")
    private String password;

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

    @NotNull
    @Schema(description = "알림 수신 동의 여부", example = "true")
    private Boolean notificationConsent;

    @NotNull
    @Schema(description = "야간 알림 수신 동의 여부", example = "false")
    private Boolean nightNotificationConsent;

}
