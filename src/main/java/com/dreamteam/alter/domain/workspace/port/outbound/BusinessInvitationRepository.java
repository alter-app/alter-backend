package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;

import java.util.List;

public interface BusinessInvitationRepository {
    void save(BusinessInvitation invitation);
    void saveAll(List<BusinessInvitation> invitations);
}
