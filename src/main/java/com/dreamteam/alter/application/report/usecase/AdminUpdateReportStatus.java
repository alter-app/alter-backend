package com.dreamteam.alter.application.report.usecase;

import com.dreamteam.alter.adapter.inbound.admin.report.dto.AdminUpdateReportStatusRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.report.entity.Report;
import com.dreamteam.alter.domain.report.port.inbound.AdminUpdateReportStatusUseCase;
import com.dreamteam.alter.domain.report.port.outbound.ReportQueryRepository;
import com.dreamteam.alter.domain.user.context.AdminActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminUpdateReportStatus")
@RequiredArgsConstructor
@Transactional
public class AdminUpdateReportStatus implements AdminUpdateReportStatusUseCase {

    private final ReportQueryRepository reportQueryRepository;

    @Override
    public void execute(Long reportId, AdminUpdateReportStatusRequestDto request, AdminActor actor) {
        Report report = reportQueryRepository.findById(reportId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "신고를 찾을 수 없습니다."));

        report.updateStatus(request.getStatus(), request.getAdminComment());
    }
}
