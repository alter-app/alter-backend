package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;

public interface BusinessInvitationRepository {
    void save(BusinessInvitation invitation);
}
