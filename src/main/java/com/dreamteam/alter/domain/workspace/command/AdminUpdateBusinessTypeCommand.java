package com.dreamteam.alter.domain.workspace.command;

public record AdminUpdateBusinessTypeCommand(
    String name,
    String description
) {}
