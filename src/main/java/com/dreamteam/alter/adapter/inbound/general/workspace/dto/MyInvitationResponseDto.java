package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import com.dreamteam.alter.domain.workspace.type.BusinessInvitationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "내가 받은 업장 초대 응답 DTO")
public class MyInvitationResponseDto {

    @Schema(description = "초대 ID", example = "1")
    private Long invitationId;

    @Schema(description = "업장명", example = "스타벅스 강남점")
    private String businessName;

    @Schema(description = "초대 상태 (만료된 PENDING은 EXPIRED로 표시)")
    private DescribedEnumDto<BusinessInvitationStatus> status;

    @Schema(description = "초대 일시", example = "2026-03-01T10:00:00")
    private LocalDateTime invitedAt;

    @Schema(description = "초대 만료 일시", example = "2026-03-08T10:00:00")
    private LocalDateTime expiresAt;

    public static MyInvitationResponseDto from(BusinessInvitation invitation, LocalDateTime now) {
        return MyInvitationResponseDto.builder()
            .invitationId(invitation.getId())
            .businessName(invitation.getWorkspace().getBusinessName())
            .status(DescribedEnumDto.of(invitation.getEffectiveStatus(now), BusinessInvitationStatus.describe()))
            .invitedAt(invitation.getCreatedAt())
            .expiresAt(invitation.getExpiresAt())
            .build();
    }
}
