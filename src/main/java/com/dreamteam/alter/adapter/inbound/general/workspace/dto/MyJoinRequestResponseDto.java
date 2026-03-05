package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.type.BusinessJoinRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyJoinRequestResponseDto {

    private Long joinRequestId;
    private String businessName;
    private BusinessJoinRequestStatus status;
    private LocalDateTime requestedAt;

    public static MyJoinRequestResponseDto from(BusinessJoinRequest request) {
        return new MyJoinRequestResponseDto(
            request.getId(),
            request.getWorkspace().getBusinessName(),
            request.getStatus(),
            request.getCreatedAt()
        );
    }
}
