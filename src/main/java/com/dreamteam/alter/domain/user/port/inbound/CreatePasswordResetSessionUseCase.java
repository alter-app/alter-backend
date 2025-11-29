package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreatePasswordResetSessionRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreatePasswordResetSessionResponseDto;

public interface CreatePasswordResetSessionUseCase {
    CreatePasswordResetSessionResponseDto execute(CreatePasswordResetSessionRequestDto request);
}

