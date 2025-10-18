package com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly;

import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReceivedSubstituteRequestListResponse {

    private Long id;

    private Long scheduleId;

    private LocalDateTime scheduleStartDateTime;

    private LocalDateTime scheduleEndDateTime;

    private String position;

    private Long workspaceId;

    private String workspaceName;

    private Long requesterId;

    private String requesterName;

    private SubstituteRequestType requestType;

    private Long acceptedWorkerId;

    private String acceptedWorkerName;

    private SubstituteRequestStatus status;

    private String requestReason;

    private LocalDateTime createdAt;

    private LocalDateTime acceptedAt;

    private LocalDateTime processedAt;

}
