package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyInvitationResponseDto {

    private Long invitationId;
    private String businessName;
    private LocalDateTime invitedAt;
    private LocalDateTime expiresAt;

    public static MyInvitationResponseDto from(BusinessInvitation invitation) {
        return new MyInvitationResponseDto(
            invitation.getId(),
            invitation.getWorkspace().getBusinessName(),
            invitation.getCreatedAt(),
            invitation.getExpiresAt()
        );
    }
}
