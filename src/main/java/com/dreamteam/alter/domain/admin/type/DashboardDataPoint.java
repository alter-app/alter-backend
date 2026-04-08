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
public class DashboardDataPoint {

    private String label;
    private long count;

    public static DashboardDataPoint of(String label, long count) {
        return DashboardDataPoint.builder()
            .label(label)
            .count(count)
            .build();
    }
}
