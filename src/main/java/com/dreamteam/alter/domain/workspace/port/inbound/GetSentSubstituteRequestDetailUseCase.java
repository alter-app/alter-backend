package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.SubstituteRequestDetailResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetSentSubstituteRequestDetailUseCase {
    SubstituteRequestDetailResponseDto execute(AppActor actor, Long requestId);
}
