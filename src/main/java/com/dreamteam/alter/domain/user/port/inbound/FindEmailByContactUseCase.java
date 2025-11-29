package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.FindEmailRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.FindEmailResponseDto;

public interface FindEmailByContactUseCase {
    FindEmailResponseDto execute(FindEmailRequestDto request);
}

