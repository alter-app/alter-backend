package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.domain.user.command.UpdateNicknameCommand;

public interface UpdateNicknameUseCase {
    void execute(UpdateNicknameCommand command);
}
