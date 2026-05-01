package com.dreamteam.alter.domain.user.command;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UnlinkSocialAccountCommand {

    private User user;
    private SocialProvider provider;

    public static UnlinkSocialAccountCommand from(User user, SocialProvider provider) {
        return UnlinkSocialAccountCommand.builder()
            .user(user)
            .provider(provider)
            .build();
    }
}
