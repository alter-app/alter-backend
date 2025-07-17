package com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly;

import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerWorkspaceListResponse {

    private Long id;

    private String businessName;

    private String fullAddress;

    private LocalDateTime createdAt;

    private WorkspaceStatus status;

}
