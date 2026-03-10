package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.*;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceJoinRequestListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceJoinRequestResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceJoinRequestListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessJoinRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getWorkspaceJoinRequestList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetWorkspaceJoinRequestList implements GetWorkspaceJoinRequestListUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final BusinessJoinRequestQueryRepository businessJoinRequestQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<WorkspaceJoinRequestResponseDto> execute(ManagerActor actor, Long workspaceId, WorkspaceJoinRequestListFilterDto filter, CursorPageRequestDto cursorPageRequest) {
        Workspace workspace = workspaceQueryRepository.findById(workspaceId)
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 업장의 관리자가 아닙니다.");
        }

        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(cursorPageRequest.cursor())) {
            cursorDto = CursorUtil.decodeCursor(cursorPageRequest.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> pageRequest = CursorPageRequest.of(cursorDto, cursorPageRequest.pageSize());

        long count = businessJoinRequestQueryRepository.countByWorkspace(workspace, filter);
        if (count == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(cursorPageRequest.pageSize(), (int) count));
        }

        List<BusinessJoinRequest> requests = businessJoinRequestQueryRepository.findByWorkspaceWithCursor(pageRequest, workspace, filter);
        if (ObjectUtils.isEmpty(requests)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(cursorPageRequest.pageSize(), (int) count));
        }

        BusinessJoinRequest last = requests.getLast();
        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
            pageRequest.pageSize(),
            (int) count
        );

        return CursorPaginatedApiResponse.of(
            pageResponseDto,
            requests.stream()
                .map(WorkspaceJoinRequestResponseDto::from)
                .toList()
        );
    }
}
