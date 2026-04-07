package com.dreamteam.alter.adapter.inbound.admin.dashboard.controller;

import com.dreamteam.alter.adapter.inbound.admin.dashboard.dto.AdminDashboardRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.dashboard.dto.AdminDashboardResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.domain.admin.port.inbound.AdminGetDashboardUseCase;
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

    private final AdminGetDashboardUseCase adminGetDashboardUseCase;

    @Override
    @GetMapping
    public ResponseEntity<CommonApiResponse<AdminDashboardResponseDto>> getDashboard(
        @Valid @ModelAttribute AdminDashboardRequestDto request
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(adminGetDashboardUseCase.execute(request)));
    }
}
