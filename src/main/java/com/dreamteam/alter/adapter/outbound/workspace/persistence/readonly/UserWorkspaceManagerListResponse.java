package com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly;

import com.dreamteam.alter.domain.workspace.type.WorkerPositionType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserWorkspaceManagerListResponse {

    private Long id;

    private UserWorkspaceWorkerResponse manager;

    @Enumerated(EnumType.STRING)
    private WorkerPositionType position;

    private LocalDateTime createdAt;

}
