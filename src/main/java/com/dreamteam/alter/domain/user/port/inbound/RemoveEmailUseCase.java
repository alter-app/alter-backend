package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.domain.user.command.RemoveEmailCommand;

public interface RemoveEmailUseCase {
    void execute(RemoveEmailCommand command);
}
