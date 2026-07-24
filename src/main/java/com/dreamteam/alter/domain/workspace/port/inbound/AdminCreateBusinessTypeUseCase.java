package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.workspace.command.AdminCreateBusinessTypeCommand;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;

public interface AdminCreateBusinessTypeUseCase {
    BusinessType execute(AdminCreateBusinessTypeCommand command);
}
