package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.domain.workspace.exception.InvitationUnavailableDetail;
import com.dreamteam.alter.domain.workspace.type.InvitationUnavailableReason;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "초대 발송 불가 번호와 사유 응답 DTO")
public class InvitationUnavailableResponseDto {

    @Schema(description = "발송 불가 전화번호", example = "01012345678")
    private String phoneNumber;

    @Schema(description = "발송 불가 사유")
    private DescribedEnumDto<InvitationUnavailableReason> reason;

    public static InvitationUnavailableResponseDto from(InvitationUnavailableDetail detail) {
        return InvitationUnavailableResponseDto.builder()
            .phoneNumber(detail.phoneNumber())
            .reason(DescribedEnumDto.of(detail.reason(), InvitationUnavailableReason.describe()))
            .build();
    }
}
