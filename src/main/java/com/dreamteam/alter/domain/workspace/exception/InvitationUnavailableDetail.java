package com.dreamteam.alter.domain.workspace.exception;

import com.dreamteam.alter.domain.workspace.type.InvitationUnavailableReason;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "초대 발송 불가 번호와 사유")
public record InvitationUnavailableDetail(
    @Schema(description = "발송 불가 전화번호", example = "01012345678")
    String phoneNumber,

    @Schema(description = "발송 불가 사유", example = "NOT_REGISTERED")
    InvitationUnavailableReason reason
) {

}
