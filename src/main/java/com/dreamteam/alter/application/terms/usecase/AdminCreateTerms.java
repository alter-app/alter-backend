package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminCreateTermsRequestDto;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.inbound.AdminCreateTermsUseCase;
import com.dreamteam.alter.domain.terms.port.outbound.TermsRepository;
import com.dreamteam.alter.domain.user.context.AdminActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminCreateTerms")
@RequiredArgsConstructor
@Transactional
public class AdminCreateTerms implements AdminCreateTermsUseCase {

    private final TermsRepository termsRepository;

    @Override
    public Long execute(AdminCreateTermsRequestDto request, AdminActor actor) {
        Terms terms = Terms.create(
                request.getType(),
                request.getVersion(),
                request.getTitle(),
                request.getNotionUrl(),
                request.isRequired()
        );
        return termsRepository.save(terms).getId();
    }
}
