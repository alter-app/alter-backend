package com.dreamteam.alter.domain.file.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.FilePresignedUrlResponseDto;
import com.dreamteam.alter.domain.user.context.AdminActor;

public interface AdminGetPresignedUrlUseCase {
    FilePresignedUrlResponseDto execute(AdminActor actor, String fileId);
}
