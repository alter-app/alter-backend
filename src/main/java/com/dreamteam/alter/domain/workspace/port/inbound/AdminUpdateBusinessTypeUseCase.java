package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.workspace.command.AdminUpdateBusinessTypeCommand;

public interface AdminUpdateBusinessTypeUseCase {
    void execute(Long id, AdminUpdateBusinessTypeCommand command);
}
