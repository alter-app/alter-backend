package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.FirebaseCustomTokenResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GenerateFirebaseCustomTokenUseCase {
    FirebaseCustomTokenResponseDto execute(AppActor actor);
}
