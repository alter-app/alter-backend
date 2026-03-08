package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "내가 받은 업장 초대 응답 DTO")
public class MyInvitationResponseDto {

    @Schema(description = "초대 ID", example = "1")
    private Long invitationId;

    @Schema(description = "업장명", example = "스타벅스 강남점")
    private String businessName;

    @Schema(description = "초대 일시", example = "2024-03-01T10:00:00")
    private LocalDateTime invitedAt;

    @Schema(description = "초대 만료 일시", example = "2024-03-08T10:00:00")
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
