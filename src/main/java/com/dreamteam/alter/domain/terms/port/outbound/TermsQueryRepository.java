package com.dreamteam.alter.domain.terms.port.outbound;

import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.type.TermsStatus;
import com.dreamteam.alter.domain.terms.type.TermsType;

import java.util.List;
import java.util.Optional;

public interface TermsQueryRepository {

    Optional<Terms> findById(Long id);

    long countByFilter(TermsType type, TermsStatus status);

    List<Terms> findByFilter(TermsType type, TermsStatus status, int page, int pageSize);

    Optional<Terms> findPublishedByType(TermsType type);

    Optional<Terms> findPublishedByTypeWithLock(TermsType type);
}
