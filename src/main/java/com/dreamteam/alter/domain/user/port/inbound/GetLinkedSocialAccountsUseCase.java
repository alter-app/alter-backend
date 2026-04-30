package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.dto.SocialAccountStatusDto;

import java.util.List;

public interface GetLinkedSocialAccountsUseCase {
    List<SocialAccountStatusDto> execute(User user);
}
