package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.GetWorkspaceScheduleResponseDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkScheduleInquiryRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetWorkspaceScheduleUseCase {
    GetWorkspaceScheduleResponseDto execute(AppActor actor, Long workspaceId, WorkScheduleInquiryRequestDto request);
}
