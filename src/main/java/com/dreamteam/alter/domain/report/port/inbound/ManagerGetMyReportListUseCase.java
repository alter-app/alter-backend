package com.dreamteam.alter.domain.report.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.report.dto.ReportListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.report.dto.ReportListResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetMyReportListUseCase {
    CursorPaginatedApiResponse<ReportListResponseDto> execute(CursorPageRequestDto request, ReportListFilterDto filter, ManagerActor actor);
}
