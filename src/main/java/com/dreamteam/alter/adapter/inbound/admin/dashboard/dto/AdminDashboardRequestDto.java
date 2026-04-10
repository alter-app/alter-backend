package com.dreamteam.alter.adapter.inbound.admin.dashboard.dto;

import com.dreamteam.alter.domain.admin.type.DashboardPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 대시보드 통계 조회 요청 DTO")
public class AdminDashboardRequestDto {

    @NotNull
    @Schema(description = "조회 기간 단위 (WEEKLY, MONTHLY, YEARLY)", example = "MONTHLY")
    private DashboardPeriod period;

    @Schema(description = "조회 연도 (null이면 현재 연도)", example = "2024")
    private Integer year;
}
