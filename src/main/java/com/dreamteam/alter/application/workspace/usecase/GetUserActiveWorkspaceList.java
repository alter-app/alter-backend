package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserWorkspaceListResponseDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceListResponse;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.GetUserActiveWorkspaceListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getUserActiveWorkspaceList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserActiveWorkspaceList implements GetUserActiveWorkspaceListUseCase {

    private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<UserWorkspaceListResponseDto> execute(
        CursorPageRequestDto request, 
        AppActor actor
    ) {
        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(request.cursor())) {
            cursorDto = CursorUtil.decodeCursor(request.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> pageRequest = CursorPageRequest.of(cursorDto, request.pageSize());

        long count = workspaceWorkerQueryRepository.getUserActiveWorkspaceCount(actor.getUser());
        if (count == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(request.pageSize(), (int) count));
        }

        List<UserWorkspaceListResponse> workspaces = workspaceWorkerQueryRepository
            .getUserActiveWorkspaceListWithCursor(pageRequest, actor.getUser());
        if (ObjectUtils.isEmpty(workspaces)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(request.pageSize(), (int) count));
        }

        UserWorkspaceListResponse last = workspaces.getLast();
        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
            pageRequest.pageSize(),
            (int) count
        );

        return CursorPaginatedApiResponse.of(
            pageResponseDto,
            workspaces.stream()
                .map(UserWorkspaceListResponseDto::from)
                .toList()
        );
    }
}
