package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.domain.user.command.UpdatePasswordCommand;

public interface UpdatePasswordUseCase {
    void execute(UpdatePasswordCommand command);
}
