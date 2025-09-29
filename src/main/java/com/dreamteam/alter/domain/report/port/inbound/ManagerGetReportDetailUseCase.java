package com.dreamteam.alter.domain.report.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.report.dto.ReportDetailResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetReportDetailUseCase {
    ReportDetailResponseDto execute(Long reportId, ManagerActor actor);
}
