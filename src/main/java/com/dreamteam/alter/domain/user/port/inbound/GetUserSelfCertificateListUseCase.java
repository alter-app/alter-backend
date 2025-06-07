package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.UserSelfCertificateListResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

import java.util.List;

public interface GetUserSelfCertificateListUseCase {
    List<UserSelfCertificateListResponseDto> execute(AppActor actor);
}
