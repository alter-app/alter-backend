package com.dreamteam.alter.application.report.usecase;

import com.dreamteam.alter.adapter.inbound.general.report.dto.ReportDetailResponseDto;
import com.dreamteam.alter.adapter.outbound.report.persistence.readonly.ReportDetailResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.report.entity.Report;
import com.dreamteam.alter.domain.report.port.inbound.ManagerGetReportDetailUseCase;
import com.dreamteam.alter.domain.report.port.outbound.ReportQueryRepository;
import com.dreamteam.alter.domain.report.type.ReporterType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("managerGetReportDetail")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerGetReportDetail implements ManagerGetReportDetailUseCase {

    private final ReportQueryRepository reportQueryRepository;

    @Override
    public ReportDetailResponseDto execute(Long reportId, ManagerActor actor) {
        // 신고자 권한 확인
        reportQueryRepository.findByIdAndReporter(
                reportId,
                ReporterType.MANAGER,
                actor.getManagerUser().getId()
            )
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "신고를 찾을 수 없습니다."));

        // target 정보와 함께 조회
        ReportDetailResponse reportDetail =
            reportQueryRepository.findByIdWithTarget(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "신고를 찾을 수 없습니다."));

        return ReportDetailResponseDto.from(reportDetail);
    }
}
