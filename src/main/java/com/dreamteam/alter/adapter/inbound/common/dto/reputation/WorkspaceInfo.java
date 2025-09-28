package com.dreamteam.alter.adapter.inbound.common.dto.reputation;

import com.dreamteam.alter.domain.workspace.entity.Workspace;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "근무 업장 정보 DTO")
public class WorkspaceInfo {

    @NotNull
    @Schema(description = "근무 업장 ID")
    private Long id;

    @NotNull
    @Schema(description = "근무 업장 이름")
    private String businessName;

    public static WorkspaceInfo of(Workspace workspace) {
        return WorkspaceInfo.builder()
            .id(workspace.getId())
            .businessName(workspace.getBusinessName())
            .build();
    }

}
