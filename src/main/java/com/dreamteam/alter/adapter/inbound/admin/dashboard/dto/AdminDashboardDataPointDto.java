package com.dreamteam.alter.adapter.inbound.admin.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "데이터 포인트")
public class AdminDashboardDataPointDto {

    @Schema(description = "레이블 (예: '1월', '1주', '2024')", example = "1월")
    private String label;

    @Schema(description = "해당 기간의 집계 수", example = "15")
    private long count;

    public static AdminDashboardDataPointDto of(String label, long count) {
        return AdminDashboardDataPointDto.builder()
            .label(label)
            .count(count)
            .build();
    }
}
