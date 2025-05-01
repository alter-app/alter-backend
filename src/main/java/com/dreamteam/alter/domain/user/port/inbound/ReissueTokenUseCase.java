package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import org.springframework.security.core.Authentication;

public interface ReissueTokenUseCase {
    GenerateTokenResponseDto execute(Authentication authentication);
}
