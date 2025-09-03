package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.LinkSocialAccountRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface LinkSocialAccountUseCase {
    void execute(AppActor actor, LinkSocialAccountRequestDto request);
}
