package com.dreamteam.alter.adapter.inbound.general.report.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.report.dto.*;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.report.port.inbound.*;
import com.dreamteam.alter.domain.user.context.AppActor;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/reports")
@PreAuthorize("hasAnyRole('USER')")
@RequiredArgsConstructor
@Validated
public class ReportController implements ReportControllerSpec {

    @Resource(name = "createReport")
    private final CreateReportUseCase createReport;

    @Resource(name = "getMyReportList")
    private final GetMyReportListUseCase getMyReportList;

    @Resource(name = "getReportDetail")
    private final GetReportDetailUseCase getReportDetail;

    @Resource(name = "cancelReport")
    private final CancelReportUseCase cancelReport;

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<Void>> createReport(
            @Valid @RequestBody CreateReportRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        createReport.execute(request, actor);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<CursorPaginatedApiResponse<ReportListResponseDto>> getMyReportList(
            CursorPageRequestDto request,
            ReportListFilterDto filter
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        return ResponseEntity.ok(getMyReportList.execute(request, filter, actor));
    }

    @Override
    @GetMapping("/me/{reportId}")
    public ResponseEntity<CommonApiResponse<ReportDetailResponseDto>> getMyReportDetail(
            @PathVariable Long reportId
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(getReportDetail.execute(reportId, actor)));
    }

    @Override
    @DeleteMapping("/me/{reportId}")
    public ResponseEntity<CommonApiResponse<Void>> cancelMyReport(
            @PathVariable Long reportId
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        cancelReport.execute(reportId, actor);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
