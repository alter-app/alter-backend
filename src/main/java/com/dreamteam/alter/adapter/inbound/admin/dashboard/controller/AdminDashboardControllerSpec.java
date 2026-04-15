package com.dreamteam.alter.adapter.inbound.admin.dashboard.controller;

import com.dreamteam.alter.adapter.inbound.admin.dashboard.dto.AdminDashboardChartResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.dashboard.dto.AdminDashboardRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.dashboard.dto.AdminDashboardWeeklySummaryResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(name = "ADMIN - 대시보드 API")
public interface AdminDashboardControllerSpec {

    @Operation(
        summary = "대시보드 차트 통계 조회",
        description = "기간(주/월/년) 단위로 업장 등록 수, 회원 가입 수 차트 데이터를 조회합니다."
    )
    ResponseEntity<CommonApiResponse<AdminDashboardChartResponseDto>> getDashboardChart(
        @Valid @ModelAttribute AdminDashboardRequestDto request
    );

    @Operation(
        summary = "대시보드 주간 요약 조회",
        description = "이번 주 신고 수, 신규 합류자 수를 조회합니다."
    )
    ResponseEntity<CommonApiResponse<AdminDashboardWeeklySummaryResponseDto>> getDashboardWeeklySummary();
}
