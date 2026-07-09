package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.application.chat.event.ChatMembershipLeftEvent;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.inbound.WorkerResignationService;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service("workerResignationService")
@RequiredArgsConstructor
@Transactional
public class WorkerResignationServiceImpl implements WorkerResignationService {

    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;
    private final WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;
    private final SubstituteRequestQueryRepository substituteRequestQueryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void resign(WorkspaceWorker worker) {
        List<WorkspaceShift> futureShifts = workspaceShiftQueryRepository
            .findFutureShiftsByAssignedWorker(worker, LocalDateTime.now());
        futureShifts.forEach(WorkspaceShift::unassignWorker);

        List<WorkspaceWorkerSchedule> fixedSchedules = workspaceWorkerScheduleQueryRepository
            .getByWorkspaceWorker(worker);
        fixedSchedules.forEach(WorkspaceWorkerSchedule::delete);

        List<SubstituteRequest> activeRequests = substituteRequestQueryRepository
            .findAllActiveByRequesterWorkerId(worker.getId());
        activeRequests.forEach(SubstituteRequest::cancel);

        List<SubstituteRequest> pendingTargetRequests = substituteRequestQueryRepository
            .findAllPendingTargetRequestsByTargetWorkerId(worker.getId());
        pendingTargetRequests.forEach(request ->
            request.cancelPendingTargetAndCancelIfNoPendingTargets(worker.getId())
        );

        worker.resign();

        eventPublisher.publishEvent(
            new ChatMembershipLeftEvent(worker.getWorkspace().getId(), worker.getUser().getId(), TokenScope.APP));
    }
}
