package com.dreamteam.alter.domain.terms.command;

import com.dreamteam.alter.domain.terms.type.TermsType;

public record AdminCreateTermsCommand(
        TermsType type,
        String version,
        String title,
        String docUrl,
        boolean required
) {}
