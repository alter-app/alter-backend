package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.LoginUserRequestDto;

public interface GenerateTokenUseCase {
    GenerateTokenResponseDto execute(LoginUserRequestDto request);
}
