package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateSignupSessionRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateSignupSessionResponseDto;

public interface CreateSignupSessionUseCase {
    CreateSignupSessionResponseDto execute(CreateSignupSessionRequestDto request);
}

