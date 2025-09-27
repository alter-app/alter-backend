package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MyScheduleResponseDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkScheduleInquiryRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.port.inbound.GetMyScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service("getMyWorkSchedule")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyWorkSchedule implements GetMyScheduleUseCase {

    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;

    @Override
    public List<MyScheduleResponseDto> execute(AppActor actor, WorkScheduleInquiryRequestDto request) {
        List<WorkspaceShift> shifts;

        if (ObjectUtils.isEmpty(request.getYear()) || ObjectUtils.isEmpty(request.getMonth())) {
            // 날짜 정보가 null인 경우: 일주일 범위 조회
            LocalDate startOfWeek = LocalDate.now();
            LocalDate endOfWeek = startOfWeek.plusDays(7);

            shifts = workspaceShiftQueryRepository.findByUserAndWeeklyRange(actor.getUser(), startOfWeek, endOfWeek);
        } else {
            // 날짜 정보가 제공된 경우: 해당 월의 일정 조회
            shifts = workspaceShiftQueryRepository.findByUserAndDateRange(actor.getUser(), request.getYear(), request.getMonth());
        }

        return shifts.stream()
            .map(MyScheduleResponseDto::of)
            .toList();
    }
}
