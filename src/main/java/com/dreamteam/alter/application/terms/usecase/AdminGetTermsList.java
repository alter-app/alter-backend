package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.inbound.AdminGetTermsListUseCase;
import com.dreamteam.alter.domain.terms.port.outbound.TermsQueryRepository;
import com.dreamteam.alter.domain.terms.query.AdminGetTermsListQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("adminGetTermsList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGetTermsList implements AdminGetTermsListUseCase {

    private final TermsQueryRepository termsQueryRepository;

    @Override
    public List<Terms> execute(AdminGetTermsListQuery query) {
        return termsQueryRepository.findByFilter(
                query.type(), query.status(), query.page(), query.pageSize());
    }

    @Override
    public long count(AdminGetTermsListQuery query) {
        return termsQueryRepository.countByFilter(query.type(), query.status());
    }
}
