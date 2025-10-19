package com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly;

import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SentSubstituteRequestListResponse {
    private Long id;
    private Long scheduleId;
    private LocalDateTime scheduleStartDateTime;
    private LocalDateTime scheduleEndDateTime;
    private String position;
    private Long workspaceId;
    private String workspaceName;
    private SubstituteRequestType requestType;
    private SubstituteRequestStatus status;
    private LocalDateTime createdAt;
}
