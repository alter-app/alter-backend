package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CheckEmailDuplicationRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CheckEmailDuplicationResponseDto;

public interface CheckEmailDuplicationUseCase {
    CheckEmailDuplicationResponseDto execute(CheckEmailDuplicationRequestDto request);
}
