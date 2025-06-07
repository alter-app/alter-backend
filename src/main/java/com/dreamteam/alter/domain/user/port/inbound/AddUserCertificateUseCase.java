package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserCertificateRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface AddUserCertificateUseCase {
    void execute(CreateUserCertificateRequestDto request, AppActor actor);
}
