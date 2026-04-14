package com.dreamteam.alter.domain.admin.type;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DashboardDataPoint {

    private String label;
    private long count;

    public static DashboardDataPoint of(String label, long count) {
        if (label == null || label.isBlank()) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT);
        }
        if (count < 0) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT);
        }
        return DashboardDataPoint.builder()
            .label(label)
            .count(count)
            .build();
    }
}
