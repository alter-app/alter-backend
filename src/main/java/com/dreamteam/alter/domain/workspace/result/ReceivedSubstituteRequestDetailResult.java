package com.dreamteam.alter.domain.workspace.result;

import java.time.LocalDateTime;

import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestTargetStatus;

public record ReceivedSubstituteRequestDetailResult(
    Long id,
    SubstituteRequestStatus status,
    SubstituteRequestTargetStatus myTargetStatus,
    Long scheduleId,
    LocalDateTime scheduleStartDateTime,
    LocalDateTime scheduleEndDateTime,
    String position,
    Long workspaceId,
    String workspaceName,
    Long requesterId,
    String requesterName,
    String requesterProfileImageUrl
) {
}
