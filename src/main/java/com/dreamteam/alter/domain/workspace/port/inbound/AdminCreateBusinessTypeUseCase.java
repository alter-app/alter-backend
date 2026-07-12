package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.workspace.command.AdminCreateBusinessTypeCommand;

public interface AdminCreateBusinessTypeUseCase {
    Long execute(AdminCreateBusinessTypeCommand command);
}
