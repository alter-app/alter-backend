package com.dreamteam.alter.domain.terms.port.outbound;

import com.dreamteam.alter.domain.terms.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsRepository extends JpaRepository<Terms, Long> {
}
