package com.dreamteam.alter.adapter.inbound.general.auth.dto;

import com.dreamteam.alter.domain.user.type.UserGender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(title = "회원가입 세션 응답 객체")
public class SignupSessionResponseDto {

    @NotNull
    @Schema(title = "회원가입 세션 ID", example = "UUID")
    private String signupSessionId;

    @Schema(title = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(title = "사용자 성별", example = "GENDER_MALE")
    private UserGender gender;

    @Schema(title = "사용자 생년월일", example = "YYYYMMDD")
    private String birthday;

    public static SignupSessionResponseDto of(String signupSessionId, SocialUserInfo socialUserInfo) {
        return SignupSessionResponseDto.builder()
            .signupSessionId(signupSessionId)
            .name(socialUserInfo.getName())
            .gender(socialUserInfo.getGender())
            .birthday(socialUserInfo.getBirthday())
            .build();
    }

}
