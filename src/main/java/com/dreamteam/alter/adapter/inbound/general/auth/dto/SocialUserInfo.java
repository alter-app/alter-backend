package com.dreamteam.alter.adapter.inbound.general.auth.dto;

import com.dreamteam.alter.domain.user.type.SocialProvider;
import com.dreamteam.alter.domain.user.type.UserGender;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class SocialUserInfo {

    private SocialProvider provider;

    private String socialId;

    private String email;

    private String name;

    private String birthday;

    private UserGender gender;

    private String refreshToken;

    public static SocialUserInfo of(
        SocialProvider provider,
        String socialId,
        String email,
        String name,
        String birthyear,
        String birthday,
        String gender
    ) {
        return SocialUserInfo.builder()
            .provider(provider)
            .socialId(socialId)
            .email(email)
            .name(name)
            .birthday(
                ObjectUtils.isEmpty(birthyear) || ObjectUtils.isEmpty(birthyear) ?
                    null : birthyear + birthday
            )
            .gender(
                ObjectUtils.isNotEmpty(gender) ?
                    gender.equals("male") ?
                        UserGender.GENDER_MALE : UserGender.GENDER_FEMALE
                    : null
            )
            .build();
    }

    public static SocialUserInfo of(
        SocialProvider provider,
        String socialId,
        String email
    ) {
        return SocialUserInfo.builder()
            .provider(provider)
            .socialId(socialId)
            .email(email)
            .build();
    }

    public static SocialUserInfo of(
        SocialProvider provider,
        String socialId,
        String email,
        String refreshToken
    ) {
        return SocialUserInfo.builder()
            .provider(provider)
            .socialId(socialId)
            .email(email)
            .refreshToken(refreshToken)
            .build();
    }

}
