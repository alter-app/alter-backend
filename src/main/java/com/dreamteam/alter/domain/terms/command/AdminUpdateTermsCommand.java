package com.dreamteam.alter.domain.terms.command;

public record AdminUpdateTermsCommand(
        String title,
        String docUrl,
        boolean required
) {}
