package com.dreamteam.alter.adapter.inbound.admin.dashboard.controller;

import com.dreamteam.alter.adapter.inbound.admin.dashboard.dto.AdminDashboardChartResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.dashboard.dto.AdminDashboardRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.dashboard.dto.AdminDashboardWeeklySummaryResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.domain.admin.port.inbound.AdminGetDashboardChartUseCase;
import com.dreamteam.alter.domain.admin.port.inbound.AdminGetDashboardWeeklySummaryUseCase;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminDashboardController implements AdminDashboardControllerSpec {

    @Resource(name = "adminGetDashboardChart")
    private AdminGetDashboardChartUseCase adminGetDashboardChartUseCase;

    @Resource(name = "adminGetDashboardWeeklySummary")
    private AdminGetDashboardWeeklySummaryUseCase adminGetDashboardWeeklySummaryUseCase;

    @Override
    @GetMapping("/chart")
    public ResponseEntity<CommonApiResponse<AdminDashboardChartResponseDto>> getDashboardChart(
        @Valid @ModelAttribute AdminDashboardRequestDto request
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(
            AdminDashboardChartResponseDto.from(
                adminGetDashboardChartUseCase.execute(request.getPeriod(), request.getYear())
            )
        ));
    }

    @Override
    @GetMapping("/weekly-summary")
    public ResponseEntity<CommonApiResponse<AdminDashboardWeeklySummaryResponseDto>> getDashboardWeeklySummary() {
        return ResponseEntity.ok(CommonApiResponse.of(
            AdminDashboardWeeklySummaryResponseDto.from(
                adminGetDashboardWeeklySummaryUseCase.execute()
            )
        ));
    }
}
