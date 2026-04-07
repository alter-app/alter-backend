package com.dreamteam.alter.adapter.inbound.admin.dashboard.controller;

import com.dreamteam.alter.adapter.inbound.admin.dashboard.dto.AdminDashboardRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.dashboard.dto.AdminDashboardResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(name = "ADMIN - 대시보드 API")
public interface AdminDashboardControllerSpec {

    @Operation(
        summary = "대시보드 통계 조회",
        description = "등록 업장 수, 가입 회원 수 (차트), 주간 신고 수, 주간 활성 사용자 수를 조회합니다."
    )
    ResponseEntity<CommonApiResponse<AdminDashboardResponseDto>> getDashboard(
        @Valid @ModelAttribute AdminDashboardRequestDto request
    );
}
