package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MonthlyWorkScheduleInquiryRequestDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MyScheduleMonthlyResponseDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MyScheduleResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.port.inbound.GetMyMonthlyScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service("getMyMonthlySchedule")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyMonthlySchedule implements GetMyMonthlyScheduleUseCase {

    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;

    @Override
    public MyScheduleMonthlyResponseDto execute(AppActor actor, @MonotonicNonNull MonthlyWorkScheduleInquiryRequestDto request) {


        List<WorkspaceShift> shifts = workspaceShiftQueryRepository.findByUserAndDateRange(
                actor.getUser(),
                request.getYear(),
                request.getMonth()
        );

        double totalWorkHours = shifts.stream()
                .mapToDouble(shift -> {
                    Duration duration = Duration.between(shift.getStartDateTime(), shift.getEndDateTime());
                    return duration.toMinutes() / 60.0;
                })
                .sum();

        List<MyScheduleResponseDto> scheduleDtos = shifts.stream()
                .map(MyScheduleResponseDto::of)
                .toList();
        return MyScheduleMonthlyResponseDto.builder()
                .totalWorkHoursInMonth(totalWorkHours)
                .schedules(scheduleDtos)
                .build();
    }
}
