package com.dreamteam.alter.domain.terms.port.outbound;

import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.type.TermsType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Terms t WHERE t.type = :type AND t.status = 'PUBLISHED'")
    Optional<Terms> findPublishedByTypeWithLock(@Param("type") TermsType type);
}
