package com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly;

import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerWorkspaceWorkerListResponse {

    private Long id;

    private WorkspaceWorkerUserResponse user;

    @Enumerated(EnumType.STRING)
    private WorkspaceWorkerStatus status;

    private LocalDate employedAt;

    private LocalDate resignedAt;

}
