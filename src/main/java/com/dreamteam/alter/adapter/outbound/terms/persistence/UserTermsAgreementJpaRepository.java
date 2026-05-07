package com.dreamteam.alter.adapter.outbound.terms.persistence;

import com.dreamteam.alter.domain.terms.entity.UserTermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTermsAgreementJpaRepository extends JpaRepository<UserTermsAgreement, Long> {
}
