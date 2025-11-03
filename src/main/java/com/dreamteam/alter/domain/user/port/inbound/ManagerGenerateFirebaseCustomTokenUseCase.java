package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.FirebaseCustomTokenResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGenerateFirebaseCustomTokenUseCase {
    FirebaseCustomTokenResponseDto execute(ManagerActor actor);
}
