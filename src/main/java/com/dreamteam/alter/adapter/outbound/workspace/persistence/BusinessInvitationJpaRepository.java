package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessInvitationJpaRepository extends JpaRepository<BusinessInvitation, Long> {
}
