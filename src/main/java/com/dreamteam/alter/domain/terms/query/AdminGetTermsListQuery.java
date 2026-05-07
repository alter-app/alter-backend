package com.dreamteam.alter.domain.terms.query;

import com.dreamteam.alter.domain.terms.type.TermsStatus;
import com.dreamteam.alter.domain.terms.type.TermsType;

public record AdminGetTermsListQuery(
        TermsType type,
        TermsStatus status,
        int page,
        int pageSize
) {}
