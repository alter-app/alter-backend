package com.dreamteam.alter.application.report.usecase;

import com.dreamteam.alter.adapter.inbound.general.report.dto.ReportDetailResponseDto;
import com.dreamteam.alter.adapter.outbound.report.persistence.readonly.ReportDetailResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.report.entity.Report;
import com.dreamteam.alter.domain.report.port.inbound.GetReportDetailUseCase;
import com.dreamteam.alter.domain.report.port.outbound.ReportQueryRepository;
import com.dreamteam.alter.domain.report.type.ReporterType;
import com.dreamteam.alter.domain.user.context.AppActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("getReportDetail")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetReportDetail implements GetReportDetailUseCase {

    private final ReportQueryRepository reportQueryRepository;

    @Override
    public ReportDetailResponseDto execute(Long reportId, AppActor actor) {
        // 신고자 권한 확인
        reportQueryRepository.findByIdAndReporter(reportId, ReporterType.USER, actor.getUser().getId())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "신고를 찾을 수 없습니다."));

        // target 정보와 함께 조회
        ReportDetailResponse reportDetail =
            reportQueryRepository.findByIdWithTarget(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "신고를 찾을 수 없습니다."));

        return ReportDetailResponseDto.from(reportDetail);
    }
}
