package com.dreamteam.alter.application.terms.service;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.outbound.TermsQueryRepository;
import com.dreamteam.alter.domain.terms.type.TermsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component("termsAgreementValidator")
@RequiredArgsConstructor
public class TermsAgreementValidator {

    private final TermsQueryRepository termsQueryRepository;

    @Transactional(readOnly = true)
    public List<Terms> validateAndResolve(Set<TermsType> agreedTermsTypes) {
        List<Terms> latestPublishedTerms = termsQueryRepository.findLatestPublishedPerType();

        boolean allRequiredAgreed = latestPublishedTerms.stream()
                .filter(Terms::isRequired)
                .allMatch(t -> agreedTermsTypes.contains(t.getType()));

        if (!allRequiredAgreed) {
            throw new CustomException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }

        return latestPublishedTerms.stream()
                .filter(t -> agreedTermsTypes.contains(t.getType()))
                .toList();
    }
}
