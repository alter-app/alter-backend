package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsDetailResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.inbound.AdminGetTermsDetailUseCase;
import com.dreamteam.alter.domain.terms.port.outbound.TermsRepository;
import com.dreamteam.alter.domain.terms.type.TermsStatus;
import com.dreamteam.alter.domain.user.context.AdminActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminGetTermsDetail")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGetTermsDetail implements AdminGetTermsDetailUseCase {

    private final TermsRepository termsRepository;

    @Override
    public AdminTermsDetailResponseDto execute(Long id, AdminActor actor) {
        Terms terms = termsRepository.findById(id)
                .filter(t -> t.getStatus() != TermsStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.TERMS_NOT_FOUND));

        return AdminTermsDetailResponseDto.from(terms);
    }
}
