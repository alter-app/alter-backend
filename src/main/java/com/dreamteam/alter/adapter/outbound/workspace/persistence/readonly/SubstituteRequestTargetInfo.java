package com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly;

import com.dreamteam.alter.domain.workspace.type.SubstituteRequestTargetStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubstituteRequestTargetInfo {
    private Long targetWorkerId;
    private String targetWorkerName;
    private SubstituteRequestTargetStatus status;
    private String rejectionReason;
    private LocalDateTime respondedAt;
}
