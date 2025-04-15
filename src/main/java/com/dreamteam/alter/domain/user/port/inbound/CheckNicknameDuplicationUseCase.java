package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CheckNicknameDuplicationRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CheckNicknameDuplicationResponseDto;

public interface CheckNicknameDuplicationUseCase {
    CheckNicknameDuplicationResponseDto execute(CheckNicknameDuplicationRequestDto request);
}
