package com.dreamteam.alter.adapter.inbound.admin.report.controller;

import com.dreamteam.alter.adapter.inbound.admin.report.dto.AdminUpdateReportStatusRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.report.dto.*;
import com.dreamteam.alter.application.aop.AdminActionContext;
import com.dreamteam.alter.domain.report.port.inbound.*;
import com.dreamteam.alter.domain.user.context.AdminActor;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/reports")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminReportController implements AdminReportControllerSpec {

    @Resource(name = "adminGetReportList")
    private final AdminGetReportListUseCase adminGetReportList;

    @Resource(name = "adminGetReportDetail")
    private final AdminGetReportDetailUseCase adminGetReportDetail;

    @Resource(name = "adminUpdateReportStatus")
    private final AdminUpdateReportStatusUseCase adminUpdateReportStatus;

    @Resource(name = "adminDeleteReport")
    private final AdminDeleteReportUseCase adminDeleteReport;

    @Override
    @GetMapping
    public ResponseEntity<CursorPaginatedApiResponse<ReportListResponseDto>> getReportList(
        CursorPageRequestDto request,
        ReportListFilterDto filter
    ) {
        AdminActor actor = AdminActionContext.getInstance().getActor();

        return ResponseEntity.ok(adminGetReportList.execute(request, filter, actor));
    }

    @Override
    @GetMapping("/{reportId}")
    public ResponseEntity<CommonApiResponse<ReportDetailResponseDto>> getReportDetail(
        @PathVariable Long reportId
    ) {
        AdminActor actor = AdminActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(adminGetReportDetail.execute(reportId, actor)));
    }

    @Override
    @PutMapping("/{reportId}/status")
    public ResponseEntity<CommonApiResponse<Void>> updateReportStatus(
        @PathVariable Long reportId,
        @Valid @RequestBody AdminUpdateReportStatusRequestDto request
    ) {
        AdminActor actor = AdminActionContext.getInstance().getActor();

        adminUpdateReportStatus.execute(reportId, request, actor);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @DeleteMapping("/{reportId}")
    public ResponseEntity<CommonApiResponse<Void>> deleteReport(
        @PathVariable Long reportId
    ) {
        adminDeleteReport.execute(reportId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
