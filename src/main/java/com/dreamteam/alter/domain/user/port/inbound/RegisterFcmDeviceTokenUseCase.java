package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.RegisterFcmDeviceTokenRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface RegisterFcmDeviceTokenUseCase {
    void execute(AppActor actor, RegisterFcmDeviceTokenRequestDto request);
}
