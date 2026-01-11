package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MonthlyWorkScheduleInquiryRequestDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MyScheduleMonthlyResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetMyMonthlyScheduleUseCase {
    MyScheduleMonthlyResponseDto execute(AppActor actor, MonthlyWorkScheduleInquiryRequestDto request);
}
