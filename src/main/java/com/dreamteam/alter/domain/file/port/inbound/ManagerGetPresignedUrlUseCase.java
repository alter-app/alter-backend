package com.dreamteam.alter.domain.file.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.file.dto.ManagerGetPresignedUrlResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetPresignedUrlUseCase {
    ManagerGetPresignedUrlResponseDto execute(ManagerActor actor, String fileId);
}
