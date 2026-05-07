package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.domain.terms.command.AdminCreateTermsCommand;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.inbound.AdminCreateTermsUseCase;
import com.dreamteam.alter.domain.terms.port.outbound.TermsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminCreateTerms")
@RequiredArgsConstructor
@Transactional
public class AdminCreateTerms implements AdminCreateTermsUseCase {

    private final TermsRepository termsRepository;

    @Override
    public Long execute(AdminCreateTermsCommand command) {
        Terms terms = Terms.create(
                command.type(),
                command.version(),
                command.title(),
                command.docUrl(),
                command.required()
        );
        return termsRepository.save(terms).getId();
    }
}
