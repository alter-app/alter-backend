package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.UserSelfCertificateResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetUserSelfCertificateUseCase {
    UserSelfCertificateResponseDto execute(AppActor actor, Long certificateId);
}
