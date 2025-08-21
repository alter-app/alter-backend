package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.*;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationRequestFilterDto;
import com.dreamteam.alter.adapter.outbound.reputation.persistence.readonly.ReputationRequestListResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.reputation.port.inbound.WorkspaceGetReputationRequestListUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("workspaceGetReputationRequestList")
@Transactional(readOnly = true)
public class WorkspaceGetReputationRequestList extends AbstractGetReputationRequestList<ManagerActor, ReputationRequestFilterDto, List<Long>>
    implements WorkspaceGetReputationRequestListUseCase {

    private final ReputationRequestQueryRepository reputationRequestQueryRepository;

    public WorkspaceGetReputationRequestList(ReputationRequestQueryRepository reputationRequestQueryRepository, com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        super(objectMapper);
        this.reputationRequestQueryRepository = reputationRequestQueryRepository;
    }

    @Override
    protected List<Long> prepare(ManagerActor actor, ReputationRequestFilterDto filter) {
        return actor.getManagerUser().getWorkspaces()
            .stream()
            .map(Workspace::getId)
            .toList();
    }

    @Override
    protected void validate(ManagerActor actor, ReputationRequestFilterDto filter, List<Long> managedWorkspaceIds) {
        if (filter == null || ObjectUtils.isEmpty(filter.getWorkspaceId())) {
            return; // 별도 검증 없음
        }
        if (!managedWorkspaceIds.contains(filter.getWorkspaceId())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "관리중인 업장이 아닙니다.");
        }
    }

    @Override
    protected boolean canProceed(ManagerActor actor, ReputationRequestFilterDto filter, List<Long> managedWorkspaceIds) {
        // 관리중인 업장이 하나도 없으면 조회 불필요
        return managedWorkspaceIds.stream().findAny().isPresent();
    }

    @Override
    protected long count(ManagerActor actor, ReputationRequestFilterDto filter, List<Long> managedWorkspaceIds) {
        if (managedWorkspaceIds.isEmpty()) { return 0; }
        return reputationRequestQueryRepository.getCountOfReputationRequestsByWorkspace(
            managedWorkspaceIds,
            filter
        );
    }

    @Override
    protected List<ReputationRequestListResponse> fetch(
        CursorPageRequest<CursorDto> pageRequest,
        ManagerActor actor,
        ReputationRequestFilterDto filter,
        List<Long> managedWorkspaceIds
    ) {
        if (managedWorkspaceIds.isEmpty()) { return List.of(); }
        return reputationRequestQueryRepository.getReputationRequestsWithCursorByWorkspace(
            pageRequest,
            managedWorkspaceIds,
            filter
        );
    }
}
