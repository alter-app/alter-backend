package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.RejectSubstituteRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface RejectSubstituteRequestUseCase {
    void execute(AppActor actor, Long requestId, RejectSubstituteRequestDto request);
}
