package com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserWorkspaceListResponse {

    private Long id;

    private Long workspaceId;

    private String businessName;

    private LocalDate employedAt;

    private LocalDateTime createdAt;

}
