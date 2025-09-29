package com.dreamteam.alter.domain.report.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.report.dto.CreateReportRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerCreateReportUseCase {
    void execute(CreateReportRequestDto request, ManagerActor actor);
}
