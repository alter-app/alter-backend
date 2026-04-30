package com.dreamteam.alter.adapter.inbound.general.user.mapper;

import com.dreamteam.alter.adapter.inbound.general.user.dto.LinkSocialAccountRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.OauthLoginTokenDto;
import com.dreamteam.alter.domain.user.command.LinkSocialAccountCommand;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.vo.OauthToken;
import org.apache.commons.lang3.ObjectUtils;

public class LinkSocialAccountCommandMapper {

    public static LinkSocialAccountCommand toCommand(User user, LinkSocialAccountRequestDto request) {
        OauthLoginTokenDto dto = request.getOauthToken();
        OauthToken oauthToken = ObjectUtils.isNotEmpty(dto) ? OauthToken.of(dto.getAccessToken(), dto.getRefreshToken()) : null;
        return LinkSocialAccountCommand.of(
            user,
            request.getProvider(),
            oauthToken,
            request.getAuthorizationCode(),
            request.getPlatformType()
        );
    }
}
