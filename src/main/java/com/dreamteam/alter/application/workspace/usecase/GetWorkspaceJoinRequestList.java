package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
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
import com.dreamteam.alter.domain.workspace.type.BusinessJoinRequestStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service("getWorkspaceJoinRequestList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetWorkspaceJoinRequestList implements GetWorkspaceJoinRequestListUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final BusinessJoinRequestQueryRepository businessJoinRequestQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<WorkspaceJoinRequestResponseDto> execute(ManagerActor actor, Long workspaceId, BusinessJoinRequestStatus status, CursorPageRequestDto cursorPageRequest) {
        Workspace workspace = workspaceQueryRepository.findById(workspaceId)
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 업장의 관리자가 아닙니다.");
        }

        CursorDto cursor = StringUtils.hasText(cursorPageRequest.cursor())
            ? CursorUtil.decodeCursor(cursorPageRequest.cursor(), CursorDto.class, objectMapper)
            : null;

        List<BusinessJoinRequest> requests = businessJoinRequestQueryRepository.findByWorkspaceWithCursor(
            workspace, status, cursor, cursorPageRequest.pageSize() + 1
        );

        boolean hasNext = requests.size() > cursorPageRequest.pageSize();
        List<BusinessJoinRequest> pageItems = hasNext
            ? requests.subList(0, cursorPageRequest.pageSize())
            : requests;

        String nextCursor = hasNext
            ? CursorUtil.encodeCursor(
                new CursorDto(pageItems.getLast().getId(), pageItems.getLast().getCreatedAt()),
                objectMapper)
            : null;

        List<WorkspaceJoinRequestResponseDto> data = pageItems.stream()
            .map(WorkspaceJoinRequestResponseDto::from)
            .toList();

        return CursorPaginatedApiResponse.of(
            CursorPageResponseDto.of(nextCursor, cursorPageRequest.pageSize(), data.size()),
            data
        );
    }
}
