package com.dreamteam.alter.domain.posting.command;

public record AdminCreatePostingKeywordCommand(
    String name,
    String description
) {}
