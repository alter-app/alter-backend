package com.dreamteam.alter.domain.admin.type;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class DashboardWeeklySummary {

    private long weeklyReportCount;
    private long weeklyNewWorkerCount;

    public static DashboardWeeklySummary of(long weeklyReportCount, long weeklyNewWorkerCount) {
        return DashboardWeeklySummary.builder()
            .weeklyReportCount(weeklyReportCount)
            .weeklyNewWorkerCount(weeklyNewWorkerCount)
            .build();
    }
}
