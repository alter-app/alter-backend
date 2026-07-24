package com.dreamteam.alter.domain.workspace.command;

public record AdminCreateBusinessTypeCommand(
    String name,
    String description
) {}
