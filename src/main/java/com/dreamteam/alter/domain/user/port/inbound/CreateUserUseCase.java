package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;

public interface CreateUserUseCase {
    GenerateTokenResponseDto execute(CreateUserRequestDto request);
}
