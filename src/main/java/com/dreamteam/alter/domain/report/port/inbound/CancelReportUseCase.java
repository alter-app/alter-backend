package com.dreamteam.alter.domain.report.port.inbound;

import com.dreamteam.alter.domain.user.context.AppActor;

public interface CancelReportUseCase {
    void execute(Long reportId, AppActor actor);
}
