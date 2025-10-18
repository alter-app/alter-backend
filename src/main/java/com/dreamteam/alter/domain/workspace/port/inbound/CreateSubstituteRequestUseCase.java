package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.CreateSubstituteRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface CreateSubstituteRequestUseCase {
    void execute(AppActor actor, Long scheduleId, CreateSubstituteRequestDto request);
}
