package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "업장 정보")
public class WorkspaceInfo {

    @Schema(description = "업장 ID", example = "1")
    private Long workspaceId;

    @Schema(description = "사업장 이름", example = "스타벅스 강남점")
    private String workspaceName;

    public static WorkspaceInfo of(
        Long workspaceId,
        String workspaceName
    ) {
        return WorkspaceInfo.builder()
            .workspaceId(workspaceId)
            .workspaceName(workspaceName)
            .build();
    }

}
