package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.type.BusinessJoinRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class WorkspaceJoinRequestResponseDto {

    private Long joinRequestId;
    private String userName;
    private String userContact;
    private BusinessJoinRequestStatus status;
    private LocalDateTime requestedAt;

    public static WorkspaceJoinRequestResponseDto from(BusinessJoinRequest request) {
        return new WorkspaceJoinRequestResponseDto(
            request.getId(),
            request.getUser().getName(),
            request.getUser().getContact(),
            request.getStatus(),
            request.getCreatedAt()
        );
    }
}
