package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CheckContactDuplicationRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CheckContactDuplicationResponseDto;

public interface CheckContactDuplicationUseCase {
    CheckContactDuplicationResponseDto execute(CheckContactDuplicationRequestDto request);
}
