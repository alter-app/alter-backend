package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.LoginWithPasswordRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;

public interface ManagerLoginWithPasswordUseCase {
    GenerateTokenResponseDto execute(LoginWithPasswordRequestDto request);
}
