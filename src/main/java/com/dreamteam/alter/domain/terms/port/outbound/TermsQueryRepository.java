package com.dreamteam.alter.domain.terms.port.outbound;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.TermsListFilterDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.type.TermsType;

import java.util.List;
import java.util.Optional;

public interface TermsQueryRepository {

    Optional<Terms> findById(Long id);

    long countByFilter(TermsListFilterDto filter);

    List<Terms> findByFilter(TermsListFilterDto filter, PageRequestDto pageRequest);

    Optional<Terms> findPublishedByType(TermsType type);
}
