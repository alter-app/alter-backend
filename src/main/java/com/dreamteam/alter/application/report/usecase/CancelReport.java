package com.dreamteam.alter.application.report.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.report.entity.Report;
import com.dreamteam.alter.domain.report.port.inbound.CancelReportUseCase;
import com.dreamteam.alter.domain.report.port.outbound.ReportQueryRepository;
import com.dreamteam.alter.domain.report.type.ReportStatus;
import com.dreamteam.alter.domain.report.type.ReporterType;
import com.dreamteam.alter.domain.user.context.AppActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("cancelReport")
@RequiredArgsConstructor
@Transactional
public class CancelReport implements CancelReportUseCase {

    private final ReportQueryRepository reportQueryRepository;

    @Override
    public void execute(Long reportId, AppActor actor) {
        Report report = reportQueryRepository.findByIdAndReporter(reportId, ReporterType.USER, actor.getUser().getId())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "신고를 찾을 수 없습니다."));

        if (!ReportStatus.cancellableStatuses().contains(report.getStatus())) {
            throw new CustomException(ErrorCode.CONFLICT, "취소할 수 없는 신고입니다.");
        }

        report.cancel();
    }
}
