package com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly;

import com.dreamteam.alter.domain.workspace.type.WorkerPositionType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserWorkspaceWorkerListResponse {

    private Long id;

    private UserWorkspaceWorkerResponse user;

    @Enumerated(EnumType.STRING)
    private WorkerPositionType position;

    private LocalDate employedAt;

    private LocalDateTime nextShiftDateTime;

    private LocalDateTime createdAt;

}
