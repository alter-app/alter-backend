package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceWorkerListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceWorkerListResponseDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceWorkerListResponse;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetWorkspaceWorkerListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("managerGetWorkspaceWorkerList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerGetWorkspaceWorkerList implements ManagerGetWorkspaceWorkerListUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;

    @Override
    public PaginatedResponseDto<ManagerWorkspaceWorkerListResponseDto> execute(
        ManagerActor actor,
        Long workspaceId,
        ManagerWorkspaceWorkerListFilterDto filter,
        PageRequestDto pageRequest
    ) {
        ManagerUser managerUser = actor.getManagerUser();

        long count = workspaceQueryRepository.getWorkspaceWorkerCount(managerUser, workspaceId, filter);
        PageResponseDto pageResponseDto = PageResponseDto.of(pageRequest, (int) count);
        if (count == 0) {
            return PaginatedResponseDto.empty(pageResponseDto);
        }

        List<ManagerWorkspaceWorkerListResponse> result =
            workspaceQueryRepository.getWorkspaceWorkerList(managerUser, workspaceId, filter, pageRequest);

        return PaginatedResponseDto.of(
            pageResponseDto,
            result.stream()
                .map(ManagerWorkspaceWorkerListResponseDto::of)
                .toList()
        );
    }

}
