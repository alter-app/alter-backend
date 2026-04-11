package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserWithSocialRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;

public interface CreateUserWithSocialUseCase {
    GenerateTokenResponseDto execute(CreateUserWithSocialRequestDto request);
}
