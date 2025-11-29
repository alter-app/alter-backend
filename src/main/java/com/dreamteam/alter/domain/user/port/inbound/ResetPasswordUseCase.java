package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.ResetPasswordRequestDto;

public interface ResetPasswordUseCase {
    void execute(ResetPasswordRequestDto request);
}

