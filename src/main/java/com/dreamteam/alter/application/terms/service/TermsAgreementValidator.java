package com.dreamteam.alter.application.terms.service;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.outbound.TermsQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component("termsAgreementValidator")
@RequiredArgsConstructor
public class TermsAgreementValidator {

    private final TermsQueryRepository termsQueryRepository;

    public List<Terms> validateAndResolve(List<Long> agreedTermsIds) {
        List<Terms> requiredTermsList = termsQueryRepository.findAllRequiredPublished();

        Set<Long> agreedSet = new HashSet<>(agreedTermsIds);
        boolean allRequiredAgreed = requiredTermsList.stream()
                .allMatch(t -> agreedSet.contains(t.getId()));

        if (!allRequiredAgreed) {
            throw new CustomException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }

        return agreedTermsIds.stream()
                .map(id -> termsQueryRepository.findById(id)
                        .orElseThrow(() -> new CustomException(
                                ErrorCode.TERMS_NOT_FOUND, "약관 ID: " + id)))
                .toList();
    }
}
