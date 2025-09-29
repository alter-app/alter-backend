package com.dreamteam.alter.domain.report.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.general.report.dto.ReportListFilterDto;
import com.dreamteam.alter.adapter.outbound.report.persistence.readonly.ReportDetailResponse;
import com.dreamteam.alter.adapter.outbound.report.persistence.readonly.ReportListResponse;
import com.dreamteam.alter.domain.report.entity.Report;
import com.dreamteam.alter.domain.report.type.ReporterType;

import java.util.List;
import java.util.Optional;

public interface ReportQueryRepository {

    Optional<Report> findById(Long id);

    Optional<ReportDetailResponse> findByIdWithTarget(Long id);

    Optional<Report> findByIdAndReporter(Long id, ReporterType reporterType, Long reporterId);

    long getCountOfReports(ReportListFilterDto filter, ReporterType reporterType, Long reporterId);

    List<ReportListResponse> getReportsWithCursor(CursorPageRequest<com.dreamteam.alter.adapter.inbound.common.dto.CursorDto> pageRequest, ReportListFilterDto filter, ReporterType reporterType, Long reporterId);

    long getAdminCountOfReports(ReportListFilterDto filter);

    List<ReportListResponse> getAdminReportsWithCursor(CursorPageRequest<com.dreamteam.alter.adapter.inbound.common.dto.CursorDto> pageRequest, ReportListFilterDto filter);
}
