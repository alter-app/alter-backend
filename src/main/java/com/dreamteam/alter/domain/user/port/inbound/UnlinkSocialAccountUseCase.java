package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.domain.user.command.UnlinkSocialAccountCommand;

public interface UnlinkSocialAccountUseCase {
    void execute(UnlinkSocialAccountCommand command);
}
