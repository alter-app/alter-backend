package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MyScheduleResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.port.inbound.GetMyScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getMyWorkSchedule")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyWorkSchedule implements GetMyScheduleUseCase {

    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;

    @Override
    public List<MyScheduleResponseDto> execute(AppActor actor, int year, int month) {
        List<WorkspaceShift> shifts = workspaceShiftQueryRepository
            .findByUserAndDateRange(actor.getUser(), year, month);
        
        return shifts.stream()
            .map(MyScheduleResponseDto::of)
            .toList();
    }
}
