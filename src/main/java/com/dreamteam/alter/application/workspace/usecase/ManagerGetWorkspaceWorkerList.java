package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceWorkerListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceWorkerListResponseDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceWorkerListResponse;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetWorkspaceWorkerListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("managerGetWorkspaceWorkerList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerGetWorkspaceWorkerList implements ManagerGetWorkspaceWorkerListUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<ManagerWorkspaceWorkerListResponseDto> execute(
        ManagerActor actor,
        Long workspaceId,
        ManagerWorkspaceWorkerListFilterDto filter,
        CursorPageRequestDto pageRequest
    ) {
        ManagerUser managerUser = actor.getManagerUser();

        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(pageRequest.cursor())) {
            cursorDto = CursorUtil.decodeCursor(pageRequest.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> cursorPageRequest = CursorPageRequest.of(cursorDto, pageRequest.pageSize());

        long count = workspaceQueryRepository.getWorkspaceWorkerCount(managerUser, workspaceId, filter);
        if (count == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequest.pageSize(), (int) count));
        }

        List<ManagerWorkspaceWorkerListResponse> result =
            workspaceQueryRepository.getWorkspaceWorkerListWithCursor(managerUser, workspaceId, filter, cursorPageRequest);
        
        if (ObjectUtils.isEmpty(result)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequest.pageSize(), (int) count));
        }

        ManagerWorkspaceWorkerListResponse last = result.getLast();
        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
            cursorPageRequest.pageSize(),
            (int) count
        );

        return CursorPaginatedApiResponse.of(
            pageResponseDto,
            result.stream()
                .map(ManagerWorkspaceWorkerListResponseDto::of)
                .toList()
        );
    }

}
