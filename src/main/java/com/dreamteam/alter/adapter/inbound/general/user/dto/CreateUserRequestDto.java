package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.domain.user.type.UserGender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "회원가입 요청 DTO")
public class CreateUserRequestDto {

    @NotBlank
    @Size(min = 64, max = 64)
    @Schema(description = "회원가입 세션 ID", example = "UUID")
    private String signupSessionId;

    @NotBlank
    @Size(min = 16, max = 64)
    @Schema(description = "닉네임", example = "유땡땡")
    private String nickname;

    @NotBlank
    @Size(min = 13, max = 13)
    @Schema(description = "휴대폰번호", example = "010-1234-5678")
    private String contact;

    @NotNull
    @Schema(description = "성별", example = "GENDER_MALE")
    private UserGender gender;

    @NotBlank
    @Size(min = 10, max = 10)
    @Schema(description = "생년월일", example = "YYYY-MM-DD")
    private String birthday;

}
