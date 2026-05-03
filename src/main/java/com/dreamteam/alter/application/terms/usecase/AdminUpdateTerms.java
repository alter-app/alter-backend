package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminUpdateTermsRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.inbound.AdminUpdateTermsUseCase;
import com.dreamteam.alter.domain.terms.port.outbound.TermsRepository;
import com.dreamteam.alter.domain.terms.type.TermsStatus;
import com.dreamteam.alter.domain.user.context.AdminActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminUpdateTerms")
@RequiredArgsConstructor
@Transactional
public class AdminUpdateTerms implements AdminUpdateTermsUseCase {

    private final TermsRepository termsRepository;

    @Override
    public void execute(Long id, AdminUpdateTermsRequestDto request, AdminActor actor) {
        Terms terms = termsRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (terms.getStatus() != TermsStatus.DRAFT) {
            throw new CustomException(ErrorCode.CONFLICT);
        }

        terms.update(request.getTitle(), request.getNotionUrl(), request.isRequired());
    }
}
