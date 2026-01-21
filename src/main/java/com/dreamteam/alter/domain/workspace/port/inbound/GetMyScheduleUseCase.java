package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.GetMyScheduleResponseDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkScheduleInquiryRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;


public interface GetMyScheduleUseCase {
    GetMyScheduleResponseDto execute(AppActor actor, WorkScheduleInquiryRequestDto request);
}
