package com.dreamteam.alter.domain.workspace.exception;

import com.dreamteam.alter.domain.workspace.type.InvitationUnavailableReason;

public record InvitationUnavailableDetail(
    String phoneNumber,
    InvitationUnavailableReason reason
) {

}
