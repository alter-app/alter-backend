package com.dreamteam.alter.adapter.inbound.general.schedule.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MyScheduleResponseDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkScheduleInquiryRequestDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkspaceScheduleResponseDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.GetMyScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceScheduleUseCase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('USER')")
@RequiredArgsConstructor
@Validated
@RequestMapping("/app/schedules")
public class UserScheduleController implements UserScheduleControllerSpec {

    @Resource(name = "getMyWorkSchedule")
    private final GetMyScheduleUseCase getMySchedule;

    @Resource(name = "getWorkspaceWorkSchedule")
    private final GetWorkspaceScheduleUseCase getWorkspaceSchedule;

    @Override
    @GetMapping("/self")
    public ResponseEntity<CommonApiResponse<List<MyScheduleResponseDto>>> getMySchedule(
        WorkScheduleInquiryRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(getMySchedule.execute(actor, request)));
    }

    @Override
    @GetMapping("/workspaces/{workspaceId}")
    public ResponseEntity<CommonApiResponse<List<WorkspaceScheduleResponseDto>>> getWorkspaceSchedule(
        @PathVariable Long workspaceId,
        WorkScheduleInquiryRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(getWorkspaceSchedule.execute(actor, workspaceId, request)));
    }
}
