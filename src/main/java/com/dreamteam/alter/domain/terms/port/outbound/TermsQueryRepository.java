package com.dreamteam.alter.domain.terms.port.outbound;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.TermsListFilterDto;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.type.TermsType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TermsQueryRepository {

    Optional<Terms> findById(Long id);

    Page<Terms> findByFilter(TermsListFilterDto filter, Pageable pageable);

    Optional<Terms> findPublishedByType(TermsType type);
}
