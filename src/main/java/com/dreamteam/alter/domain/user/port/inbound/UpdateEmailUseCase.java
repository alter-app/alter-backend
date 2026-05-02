package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.domain.user.command.UpdateEmailCommand;

public interface UpdateEmailUseCase {
    void execute(UpdateEmailCommand command);
}
