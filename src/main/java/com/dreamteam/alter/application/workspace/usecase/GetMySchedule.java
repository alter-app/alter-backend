package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MyScheduleInquiryResponseDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MyScheduleResponseDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkScheduleInquiryRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.port.inbound.GetMyScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service("getMySchedule")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMySchedule implements GetMyScheduleUseCase {

    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;

    @Override
    public MyScheduleInquiryResponseDto execute(AppActor actor, WorkScheduleInquiryRequestDto request) {

        List<WorkspaceShift> shifts;

        if (ObjectUtils.isNotEmpty(request.getYear()) && ObjectUtils.isNotEmpty(request.getMonth()) && ObjectUtils.isNotEmpty(request.getDay())) {
            // 1. 년/월/일 포함 -> 일별 조회
            shifts = workspaceShiftQueryRepository.findByUserAndDate(
                    actor.getUser(),
                    request.getYear(),
                    request.getMonth(),
                    request.getDay()
            );

        } else if (ObjectUtils.isNotEmpty(request.getYear()) && ObjectUtils.isNotEmpty(request.getMonth())) {
            // 2. 년/월 포함 -> 월별 조회
            shifts = workspaceShiftQueryRepository.findByUserAndDateRange(
                    actor.getUser(),
                    request.getYear(),
                    request.getMonth()
            );

        } else if (ObjectUtils.isEmpty(request.getYear()) && ObjectUtils.isEmpty(request.getMonth()) && ObjectUtils.isEmpty(request.getDay())) {
            // 3. 인자 없음 -> 이번 주 스케줄 조회 (월~일)
            LocalDate now = LocalDate.now();
            LocalDate startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            shifts = workspaceShiftQueryRepository.findByUserAndWeeklyRange(
                    actor.getUser(),
                    startOfWeek,
                    endOfWeek
            );

        } else {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "연단위 요청은 불가능합니다.");
        }

        double totalWorkHours = shifts.stream()
                .mapToDouble(shift -> {
                    Duration duration = Duration.between(shift.getStartDateTime(), shift.getEndDateTime());
                    return duration.toMinutes() / 60.0;
                })
                .sum();

        List<MyScheduleResponseDto> scheduleDtos = shifts.stream()
                .map(MyScheduleResponseDto::of)
                .toList();

        return MyScheduleInquiryResponseDto.of(totalWorkHours, scheduleDtos);
    }
}
