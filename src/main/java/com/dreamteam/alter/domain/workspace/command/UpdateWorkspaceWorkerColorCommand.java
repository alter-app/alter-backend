package com.dreamteam.alter.domain.workspace.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateWorkspaceWorkerColorCommand {

    private String colorCode;

    public static UpdateWorkspaceWorkerColorCommand of(String colorCode) {
        return UpdateWorkspaceWorkerColorCommand.builder()
            .colorCode(colorCode)
            .build();
    }
}
