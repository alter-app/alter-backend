package com.dreamteam.alter.domain.file.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.file.dto.AdminGetPresignedUrlResponseDto;
import com.dreamteam.alter.domain.user.context.AdminActor;

public interface AdminGetPresignedUrlUseCase {
    AdminGetPresignedUrlResponseDto execute(AdminActor actor, String fileId);
}
