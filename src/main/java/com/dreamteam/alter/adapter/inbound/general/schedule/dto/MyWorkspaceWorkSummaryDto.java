package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import com.dreamteam.alter.domain.workspace.entity.Workspace;
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
@Schema(description = "업장별 근무시간·예상 급여")
public class MyWorkspaceWorkSummaryDto {

    @Schema(description = "워크스페이스 ID", example = "1")
    private Long workspaceId;

    @Schema(description = "워크스페이스명", example = "스타벅스 강남점")
    private String workspaceName;

    @Schema(description = "해당 업장 총 근무 시간", example = "24.0")
    private double totalWorkHours;

    @Schema(description = "해당 업장 예상 급여 (최저시급 기준)", example = "247680")
    private long estimatedSalary;

    public static MyWorkspaceWorkSummaryDto of(Workspace workspace, double totalWorkHours, long estimatedSalary) {
        return MyWorkspaceWorkSummaryDto.builder()
            .workspaceId(workspace.getId())
            .workspaceName(workspace.getBusinessName())
            .totalWorkHours(totalWorkHours)
            .estimatedSalary(estimatedSalary)
            .build();
    }
}
