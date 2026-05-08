package com.dreamteam.alter.domain.terms.result;

import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.type.TermsType;

import java.time.LocalDateTime;

public record GetPublishedTermsListResult(
        Long id,
        TermsType type,
        String title,
        String docUrl,
        boolean required,
        String version,
        LocalDateTime effectiveAt
) {
    public static GetPublishedTermsListResult from(Terms terms) {
        return new GetPublishedTermsListResult(
                terms.getId(),
                terms.getType(),
                terms.getTitle(),
                terms.getDocUrl(),
                terms.isRequired(),
                terms.getVersion(),
                terms.getEffectiveAt()
        );
    }
}
