package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyInvitationResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.type.BusinessInvitationStatus;

import java.time.LocalDate;

public interface GetMyInvitationListUseCase {
    CursorPaginatedApiResponse<MyInvitationResponseDto> execute(AppActor actor, BusinessInvitationStatus status, LocalDate from, LocalDate to, CursorPageRequestDto cursorPageRequest);
}
