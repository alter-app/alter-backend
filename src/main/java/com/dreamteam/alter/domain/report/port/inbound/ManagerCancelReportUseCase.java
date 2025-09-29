package com.dreamteam.alter.domain.report.port.inbound;

import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerCancelReportUseCase {
    void execute(Long reportId, ManagerActor actor);
}
