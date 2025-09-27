package com.dreamteam.alter.adapter.inbound.common.dto;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "근무자 요약 정보")
public class WorkerSummaryDto {
    
    @Schema(description = "근무자 ID", example = "1")
    private Long workerId;
    
    @Schema(description = "근무자명", example = "김알바")
    private String workerName;

    public static WorkerSummaryDto of(WorkspaceWorker workspaceWorker) {
        if (ObjectUtils.isEmpty(workspaceWorker)) {
            return null;
        }
        
        return WorkerSummaryDto.builder()
            .workerId(workspaceWorker.getId())
            .workerName(workspaceWorker.getUser().getName())
            .build();
    }
}
