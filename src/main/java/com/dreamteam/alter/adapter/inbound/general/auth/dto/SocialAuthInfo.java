package com.dreamteam.alter.adapter.inbound.general.auth.dto;

import com.dreamteam.alter.domain.user.type.SocialProvider;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class SocialAuthInfo {

    private SocialProvider provider;

    private String socialId;

    private String email;

    private String refreshToken;

    public static SocialAuthInfo of(
        SocialProvider provider,
        String socialId,
        String email,
        String refreshToken
    ) {
        return SocialAuthInfo.builder()
            .provider(provider)
            .socialId(socialId)
            .email(email)
            .refreshToken(refreshToken)
            .build();
    }

}
