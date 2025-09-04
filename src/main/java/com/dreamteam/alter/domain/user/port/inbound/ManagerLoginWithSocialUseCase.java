package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.SocialLoginRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;

public interface ManagerLoginWithSocialUseCase {
    GenerateTokenResponseDto execute(SocialLoginRequestDto request);
}
