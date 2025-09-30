package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserWorkspaceWorkerListResponseDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceWorkerListResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.GetUserWorkspaceWorkerListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getUserWorkspaceWorkerList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserWorkspaceWorkerList implements GetUserWorkspaceWorkerListUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<UserWorkspaceWorkerListResponseDto> execute(
        AppActor actor,
        Long workspaceId,
        CursorPageRequestDto pageRequest
    ) {
        // 사용자가 해당 업장에서 근무중인지 확인
        if (!workspaceQueryRepository.isUserActiveWorkerInWorkspace(actor.getUser(), workspaceId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 업장에서 근무중이 아닙니다.");
        }

        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(pageRequest.cursor())) {
            cursorDto = CursorUtil.decodeCursor(pageRequest.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> cursorPageRequest = CursorPageRequest.of(cursorDto, pageRequest.pageSize());

        long count = workspaceQueryRepository.getUserWorkspaceWorkerCount(actor.getUser(), workspaceId);
        if (count == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequest.pageSize(), (int) count));
        }

        List<UserWorkspaceWorkerListResponse> result =
            workspaceQueryRepository.getUserWorkspaceWorkerListWithCursor(actor.getUser(), workspaceId, cursorPageRequest);
        
        if (ObjectUtils.isEmpty(result)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequest.pageSize(), (int) count));
        }

        UserWorkspaceWorkerListResponse last = result.getLast();
        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getNextShiftDateTime()), objectMapper),
            cursorPageRequest.pageSize(),
            (int) count
        );

        return CursorPaginatedApiResponse.of(
            pageResponseDto,
            result.stream()
                .map(UserWorkspaceWorkerListResponseDto::of)
                .toList()
        );
    }

}
