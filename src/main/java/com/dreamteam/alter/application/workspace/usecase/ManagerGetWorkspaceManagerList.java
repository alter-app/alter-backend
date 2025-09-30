package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceManagerListResponse;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceManagerListResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetWorkspaceManagerListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("managerGetWorkspaceManagerList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerGetWorkspaceManagerList implements ManagerGetWorkspaceManagerListUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<ManagerWorkspaceManagerListResponseDto> execute(
        ManagerActor actor,
        Long workspaceId,
        CursorPageRequestDto pageRequest
    ) {
        ManagerUser managerUser = actor.getManagerUser();

        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(pageRequest.cursor())) {
            cursorDto = CursorUtil.decodeCursor(pageRequest.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> cursorPageRequest = CursorPageRequest.of(cursorDto, pageRequest.pageSize());

        List<ManagerWorkspaceManagerListResponse> result =
            workspaceQueryRepository.getManagerWorkspaceManagerListWithCursor(managerUser, workspaceId, cursorPageRequest);
        
        if (ObjectUtils.isEmpty(result)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequest.pageSize(), 0));
        }

        ManagerWorkspaceManagerListResponse last = result.getLast();
        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
            cursorPageRequest.pageSize(),
            result.size()
        );

        return CursorPaginatedApiResponse.of(
            pageResponseDto,
            result.stream()
                .map(ManagerWorkspaceManagerListResponseDto::of)
                .toList()
        );
    }

}
