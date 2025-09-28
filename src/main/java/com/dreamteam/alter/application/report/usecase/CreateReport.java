package com.dreamteam.alter.application.report.usecase;

import com.dreamteam.alter.adapter.inbound.general.report.dto.CreateReportRequestDto;
import com.dreamteam.alter.domain.report.entity.Report;
import com.dreamteam.alter.domain.report.port.inbound.CreateReportUseCase;
import com.dreamteam.alter.domain.report.port.outbound.ReportRepository;
import com.dreamteam.alter.domain.report.type.ReporterType;
import com.dreamteam.alter.domain.user.context.AppActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("createReport")
@RequiredArgsConstructor
@Transactional
public class CreateReport implements CreateReportUseCase {

    private final ReportRepository reportRepository;

    @Override
    public void execute(CreateReportRequestDto request, AppActor actor) {
        reportRepository.save(Report.create(
            ReporterType.USER,
            actor.getUser().getId(),
            request.getTargetType(),
            request.getTargetId(),
            request.getReason()
        ));
    }
}
