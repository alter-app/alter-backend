package com.dreamteam.alter.application.report.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.report.entity.Report;
import com.dreamteam.alter.domain.report.port.inbound.ManagerCancelReportUseCase;
import com.dreamteam.alter.domain.report.port.outbound.ReportQueryRepository;
import com.dreamteam.alter.domain.report.type.ReportStatus;
import com.dreamteam.alter.domain.report.type.ReporterType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("managerCancelReport")
@RequiredArgsConstructor
@Transactional
public class ManagerCancelReport implements ManagerCancelReportUseCase {

    private final ReportQueryRepository reportQueryRepository;

    @Override
    public void execute(Long reportId, ManagerActor actor) {
        Report report = reportQueryRepository.findByIdAndReporter(
                reportId,
                ReporterType.MANAGER,
                actor.getManagerUser().getId()
            )
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "신고를 찾을 수 없습니다."));

        if (!ReportStatus.cancellableStatuses().contains(report.getStatus())) {
            throw new CustomException(ErrorCode.CONFLICT, "취소할 수 없는 신고입니다.");
        }

        report.cancel();
    }
}
