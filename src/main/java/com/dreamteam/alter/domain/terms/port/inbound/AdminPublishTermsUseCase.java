package com.dreamteam.alter.domain.terms.port.inbound;

import com.dreamteam.alter.domain.user.context.AdminActor;

public interface AdminPublishTermsUseCase {

    void execute(Long id, AdminActor actor);
}
