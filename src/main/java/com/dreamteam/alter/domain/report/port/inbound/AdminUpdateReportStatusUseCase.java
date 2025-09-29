package com.dreamteam.alter.domain.report.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.report.dto.AdminUpdateReportStatusRequestDto;
import com.dreamteam.alter.domain.user.context.AdminActor;

public interface AdminUpdateReportStatusUseCase {
    void execute(Long reportId, AdminUpdateReportStatusRequestDto request, AdminActor actor);
}
