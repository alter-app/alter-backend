package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.UpdateUserCertificateRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface UpdateUserSelfCertificateUseCase {
    void execute(AppActor actor, Long certificateId, UpdateUserCertificateRequestDto request);
}
