package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerSubstituteRequestResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetSubstituteRequestListUseCase;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerSubstituteRequestListResponse;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerSubstituteRequestListFilterDto;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("managerGetSubstituteRequestList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerGetSubstituteRequestList implements ManagerGetSubstituteRequestListUseCase {

    private final SubstituteRequestQueryRepository substituteRequestQueryRepository;
    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<ManagerSubstituteRequestResponseDto> execute(
        ManagerActor actor,
        Long workspaceId,
        ManagerSubstituteRequestListFilterDto filter,
        CursorPageRequestDto pageRequestDto
    ) {
        // 워크스페이스 조회 및 권한 확인
        Workspace workspace = workspaceQueryRepository.findById(workspaceId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 워크스페이스입니다."));

        if (!workspace.getManagerUser().equals(actor.getManagerUser())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "관리 중인 업장이 아닙니다.");
        }

        // 상태 검증: 매니저가 조회할 수 있는 상태인지 확인
        SubstituteRequestStatus status = ObjectUtils.isNotEmpty(filter) ? filter.getStatus() : null;
        if (ObjectUtils.isNotEmpty(status) && !SubstituteRequestStatus.isManagerViewable(status)) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "조회할 수 없는 상태입니다.");
        }

        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(pageRequestDto.cursor())) {
            cursorDto = CursorUtil.decodeCursor(pageRequestDto.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> pageRequest = CursorPageRequest.of(cursorDto, pageRequestDto.pageSize());

        long totalCount = substituteRequestQueryRepository.getManagerRequestCount(workspaceId, status);
        if (totalCount == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequestDto.pageSize(), (int) totalCount));
        }

        List<ManagerSubstituteRequestListResponse> requestList = substituteRequestQueryRepository
            .getManagerRequestListWithCursor(workspaceId, status, pageRequest);
        
        if (ObjectUtils.isEmpty(requestList)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequestDto.pageSize(), (int) totalCount));
        }

        List<ManagerSubstituteRequestResponseDto> requests = requestList
            .stream()
            .map(ManagerSubstituteRequestResponseDto::of)
            .toList();

        ManagerSubstituteRequestListResponse last = requestList.getLast();

        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
            pageRequest.pageSize(),
            (int) totalCount
        );

        return CursorPaginatedApiResponse.of(pageResponseDto, requests);
    }
}
