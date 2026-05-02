package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.domain.user.command.GetUserSelfInfoCommand;
import com.dreamteam.alter.domain.user.result.GetUserSelfInfoResult;

public interface GetUserSelfInfoUseCase {
    GetUserSelfInfoResult execute(GetUserSelfInfoCommand command);
}
