package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.domain.user.command.LinkSocialAccountCommand;

public interface LinkSocialAccountUseCase {
    void execute(LinkSocialAccountCommand command);
}
