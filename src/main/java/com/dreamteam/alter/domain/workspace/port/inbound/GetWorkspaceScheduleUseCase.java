package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkspaceScheduleResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

import java.util.List;

public interface GetWorkspaceScheduleUseCase {
    List<WorkspaceScheduleResponseDto> execute(AppActor actor, Long workspaceId, int year, int month);
}
