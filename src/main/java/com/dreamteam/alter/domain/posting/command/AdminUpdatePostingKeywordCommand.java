package com.dreamteam.alter.domain.posting.command;

public record AdminUpdatePostingKeywordCommand(
    String name,
    String description
) {}
