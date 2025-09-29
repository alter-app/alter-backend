package com.dreamteam.alter.adapter.inbound.manager.report.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.report.dto.*;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.report.port.inbound.ManagerCreateReportUseCase;
import com.dreamteam.alter.domain.report.port.inbound.ManagerGetMyReportListUseCase;
import com.dreamteam.alter.domain.report.port.inbound.ManagerGetReportDetailUseCase;
import com.dreamteam.alter.domain.report.port.inbound.ManagerCancelReportUseCase;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manager/reports")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class ManagerReportController implements ManagerReportControllerSpec {

    @Resource(name = "managerCreateReport")
    private final ManagerCreateReportUseCase createReport;

    @Resource(name = "managerGetMyReportList")
    private final ManagerGetMyReportListUseCase getMyReportList;

    @Resource(name = "managerGetReportDetail")
    private final ManagerGetReportDetailUseCase getReportDetail;

    @Resource(name = "managerCancelReport")
    private final ManagerCancelReportUseCase cancelReport;

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<Void>> createReport(
            @Valid @RequestBody CreateReportRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        createReport.execute(request, actor);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<CursorPaginatedApiResponse<ReportListResponseDto>> getMyReportList(
            CursorPageRequestDto request,
            ReportListFilterDto filter
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        return ResponseEntity.ok(getMyReportList.execute(request, filter, actor));
    }

    @Override
    @GetMapping("/me/{reportId}")
    public ResponseEntity<CommonApiResponse<ReportDetailResponseDto>> getMyReportDetail(
            @PathVariable Long reportId
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(getReportDetail.execute(reportId, actor)));
    }

    @Override
    @DeleteMapping("/me/{reportId}")
    public ResponseEntity<CommonApiResponse<Void>> cancelMyReport(
            @PathVariable Long reportId
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        cancelReport.execute(reportId, actor);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
