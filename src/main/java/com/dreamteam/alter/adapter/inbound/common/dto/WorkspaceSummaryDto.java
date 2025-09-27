package com.dreamteam.alter.adapter.inbound.common.dto;

import com.dreamteam.alter.domain.workspace.entity.Workspace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "워크스페이스 요약 정보")
public class WorkspaceSummaryDto {
    
    @Schema(description = "워크스페이스 ID", example = "1")
    private Long workspaceId;
    
    @Schema(description = "워크스페이스명", example = "스타벅스 강남점")
    private String workspaceName;

    public static WorkspaceSummaryDto of(Workspace workspace) {
        return WorkspaceSummaryDto.builder()
            .workspaceId(workspace.getId())
            .workspaceName(workspace.getBusinessName())
            .build();
    }
}
