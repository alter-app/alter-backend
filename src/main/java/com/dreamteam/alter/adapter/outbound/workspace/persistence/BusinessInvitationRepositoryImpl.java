package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessInvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BusinessInvitationRepositoryImpl implements BusinessInvitationRepository {

    private final BusinessInvitationJpaRepository businessInvitationJpaRepository;

    @Override
    public void save(BusinessInvitation invitation) {
        businessInvitationJpaRepository.save(invitation);
    }
}
