package com.dreamteam.alter.application.report.usecase;

import com.dreamteam.alter.adapter.inbound.general.report.dto.CreateReportRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.report.entity.Report;
import com.dreamteam.alter.domain.report.port.inbound.ManagerCreateReportUseCase;
import com.dreamteam.alter.domain.report.port.outbound.ReportRepository;
import com.dreamteam.alter.domain.report.type.ReportTargetType;
import com.dreamteam.alter.domain.report.type.ReporterType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("managerCreateReport")
@RequiredArgsConstructor
@Transactional
public class ManagerCreateReport implements ManagerCreateReportUseCase {

    private final ReportRepository reportRepository;

    @Override
    public void execute(CreateReportRequestDto request, ManagerActor actor) {
        if (ReportTargetType.WORKSPACE.equals(request.getTargetType())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "매니저는 업장에 대한 신고를 작성할 수 없습니다.");
        }
        
        reportRepository.save(Report.create(
            ReporterType.MANAGER,
            actor.getManagerUser().getId(),
            request.getTargetType(),
            request.getTargetId(),
            request.getReason()
        ));
    }
}
