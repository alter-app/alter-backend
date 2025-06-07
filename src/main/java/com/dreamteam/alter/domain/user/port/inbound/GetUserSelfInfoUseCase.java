package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.UserSelfInfoResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetUserSelfInfoUseCase {
    UserSelfInfoResponseDto execute(AppActor actor);
}
