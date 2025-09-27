package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MyScheduleResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

import java.util.List;

public interface GetMyScheduleUseCase {
    List<MyScheduleResponseDto> execute(AppActor actor, int year, int month);
}
