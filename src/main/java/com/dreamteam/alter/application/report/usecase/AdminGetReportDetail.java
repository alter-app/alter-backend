package com.dreamteam.alter.application.report.usecase;

import com.dreamteam.alter.adapter.inbound.general.report.dto.ReportDetailResponseDto;
import com.dreamteam.alter.adapter.outbound.report.persistence.readonly.ReportDetailResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.report.port.inbound.AdminGetReportDetailUseCase;
import com.dreamteam.alter.domain.report.port.outbound.ReportQueryRepository;
import com.dreamteam.alter.domain.user.context.AdminActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminGetReportDetail")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGetReportDetail implements AdminGetReportDetailUseCase {

    private final ReportQueryRepository reportQueryRepository;

    @Override
    public ReportDetailResponseDto execute(Long reportId, AdminActor actor) {
        ReportDetailResponse reportDetail =
            reportQueryRepository.findByIdWithTarget(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "신고를 찾을 수 없습니다."));

        return ReportDetailResponseDto.from(reportDetail);
    }
}
