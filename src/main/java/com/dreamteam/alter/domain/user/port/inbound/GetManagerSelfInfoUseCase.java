package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.user.dto.ManagerSelfInfoResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface GetManagerSelfInfoUseCase {
    ManagerSelfInfoResponseDto execute(ManagerActor actor);
}
