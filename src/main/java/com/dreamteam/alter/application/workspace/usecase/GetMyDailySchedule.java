package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.DailyWorkScheduleInquiryRequestDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MyScheduleResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.port.inbound.GetMyDailyScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("getMyDailySchedule")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyDailySchedule implements GetMyDailyScheduleUseCase {

    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;

    @Override
    public List<MyScheduleResponseDto> execute(AppActor actor, @MonotonicNonNull DailyWorkScheduleInquiryRequestDto request) {

        List<WorkspaceShift> dailyShifts = workspaceShiftQueryRepository.findByUserAndDate(
                actor.getUser(),
                request.getYear(),
                request.getMonth(),
                request.getDay()
        );

        return dailyShifts.stream()
                .map(MyScheduleResponseDto::of)
                .collect(Collectors.toList());
    }
}
