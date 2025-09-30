package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserWorkspaceManagerListResponseDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceManagerListResponse;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.workspace.port.inbound.GetUserWorkspaceManagerListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getUserWorkspaceManagerList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserWorkspaceManagerList implements GetUserWorkspaceManagerListUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<UserWorkspaceManagerListResponseDto> execute(
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

        long count = workspaceQueryRepository.getUserWorkspaceManagerCount(actor.getUser(), workspaceId);
        if (count == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequest.pageSize(), (int) count));
        }

        List<UserWorkspaceManagerListResponse> result =
            workspaceQueryRepository.getUserWorkspaceManagerListWithCursor(actor.getUser(), workspaceId, cursorPageRequest);
        
        if (ObjectUtils.isEmpty(result)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequest.pageSize(), (int) count));
        }

        UserWorkspaceManagerListResponse last = result.getLast();
        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
            cursorPageRequest.pageSize(),
            (int) count
        );

        return CursorPaginatedApiResponse.of(
            pageResponseDto,
            result.stream()
                .map(UserWorkspaceManagerListResponseDto::of)
                .toList()
        );
    }

}
