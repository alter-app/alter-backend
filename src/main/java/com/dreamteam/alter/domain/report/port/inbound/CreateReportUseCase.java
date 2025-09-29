package com.dreamteam.alter.domain.report.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.report.dto.CreateReportRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface CreateReportUseCase {
    void execute(CreateReportRequestDto request, AppActor actor);
}
