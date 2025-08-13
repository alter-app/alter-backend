package com.dreamteam.alter.adapter.outbound.reputation.persistence.readonly;

import com.dreamteam.alter.domain.reputation.type.ReputationRequestStatus;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestType;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReputationRequestListResponse {
    private Long id;
    private ReputationRequestType requestType;
    private ReputationType requesterType;
    private Long requesterId;
    private String requesterName;
    private ReputationType targetType;
    private Long targetId;
    private String targetName;
    private ReputationRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
