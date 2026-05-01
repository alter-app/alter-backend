package com.dreamteam.alter.domain.user.command;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.type.PlatformType;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import com.dreamteam.alter.domain.user.vo.OauthToken;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkSocialAccountCommand {

    private User user;
    private SocialProvider provider;
    private OauthToken oauthToken;
    private String authorizationCode;
    private PlatformType platformType;

    public static LinkSocialAccountCommand of(
        User user,
        SocialProvider provider,
        OauthToken oauthToken,
        String authorizationCode,
        PlatformType platformType
    ) {
        return LinkSocialAccountCommand.builder()
            .user(user)
            .provider(provider)
            .oauthToken(oauthToken)
            .authorizationCode(authorizationCode)
            .platformType(platformType)
            .build();
    }
}
