package com.dreamteam.alter.domain.file.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.FilePresignedUrlResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetPresignedUrlUseCase {
    FilePresignedUrlResponseDto execute(ManagerActor actor, String fileId);
}
