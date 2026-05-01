package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.result.SocialAccountStatusResult;

import java.util.List;

public interface GetLinkedSocialAccountsUseCase {
    List<SocialAccountStatusResult> execute(User user);
}
