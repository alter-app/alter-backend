package com.dreamteam.alter.application.report.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.report.entity.Report;
import com.dreamteam.alter.domain.report.port.inbound.AdminDeleteReportUseCase;
import com.dreamteam.alter.domain.report.port.outbound.ReportQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminDeleteReport")
@RequiredArgsConstructor
@Transactional
public class AdminDeleteReport implements AdminDeleteReportUseCase {

    private final ReportQueryRepository reportQueryRepository;

    @Override
    public void execute(Long reportId) {
        Report report = reportQueryRepository.findById(reportId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "신고를 찾을 수 없습니다."));

        report.delete();
    }
}
