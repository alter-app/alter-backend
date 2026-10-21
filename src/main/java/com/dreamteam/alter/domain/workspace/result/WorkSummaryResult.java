package com.dreamteam.alter.domain.workspace.result;

import com.dreamteam.alter.common.constants.WorkspaceConstants;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;

import java.util.List;

// 근무 목록 → 근무시간 합계 → 예상 급여(최저시급 기준). 홈·업장 상세·사장님 인건비가 같은 식을 쓰도록 여기 한 곳에 둔다
public record WorkSummaryResult(
    double totalWorkHours,
    long estimatedSalary
) {
    public static WorkSummaryResult from(List<WorkspaceShift> shifts) {
        double totalWorkHours = shifts.stream()
            .mapToDouble(WorkspaceShift::getWorkHours)
            .sum();
        return new WorkSummaryResult(totalWorkHours, Math.round(totalWorkHours * WorkspaceConstants.MINIMUM_HOURLY_WAGE));
    }
}
