package com.dreamteam.alter.domain.workspace.result;

import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;

import java.util.List;

public record WorkspaceWorkSummaryResult(
    Workspace workspace,
    double totalWorkHours,
    long estimatedSalary
) {
    public static WorkspaceWorkSummaryResult of(Workspace workspace, List<WorkspaceShift> shifts) {
        WorkSummaryResult summary = WorkSummaryResult.from(shifts);
        return new WorkspaceWorkSummaryResult(workspace, summary.totalWorkHours(), summary.estimatedSalary());
    }
}
