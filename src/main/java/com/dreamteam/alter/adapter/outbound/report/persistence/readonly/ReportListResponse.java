package com.dreamteam.alter.adapter.outbound.report.persistence.readonly;

import com.dreamteam.alter.domain.report.type.ReportStatus;
import com.dreamteam.alter.domain.report.type.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportListResponse {
    private Long id;
    private ReportTargetType targetType;
    private String targetName;
    private ReportStatus status;
    private LocalDateTime createdAt;
}
