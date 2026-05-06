package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsDetailResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.inbound.AdminGetTermsDetailUseCase;
import com.dreamteam.alter.domain.terms.port.outbound.TermsQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminGetTermsDetail")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGetTermsDetail implements AdminGetTermsDetailUseCase {

    private final TermsQueryRepository termsQueryRepository;

    @Override
    public AdminTermsDetailResponseDto execute(Long id) {
        Terms terms = termsQueryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return AdminTermsDetailResponseDto.from(terms);
    }
}
