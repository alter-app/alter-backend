package com.dreamteam.alter.domain.file.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.FilePresignedUrlResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface AppGetPresignedUrlUseCase {
    FilePresignedUrlResponseDto execute(AppActor actor, String fileId);
}
