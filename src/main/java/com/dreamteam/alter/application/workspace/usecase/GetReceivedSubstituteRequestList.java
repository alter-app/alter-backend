package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.GetReceivedSubstituteRequestsFilterDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.ReceivedSubstituteRequestResponseDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ReceivedSubstituteRequestListResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.GetReceivedSubstituteRequestListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getReceivedSubstituteRequestList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetReceivedSubstituteRequestList implements GetReceivedSubstituteRequestListUseCase {

    private final SubstituteRequestQueryRepository substituteRequestQueryRepository;
    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<ReceivedSubstituteRequestResponseDto> execute(
        AppActor actor,
        GetReceivedSubstituteRequestsFilterDto filter,
        CursorPageRequestDto pageRequestDto
    ) {
        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(pageRequestDto.cursor())) {
            cursorDto = CursorUtil.decodeCursor(pageRequestDto.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> pageRequest = CursorPageRequest.of(cursorDto, pageRequestDto.pageSize());

        long totalCount = substituteRequestQueryRepository.getReceivedRequestCount(actor.getUser(), filter);
        if (totalCount == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequestDto.pageSize(), (int) totalCount));
        }

        List<ReceivedSubstituteRequestListResponse> requestList = substituteRequestQueryRepository
            .getReceivedRequestListWithCursor(actor.getUser(), filter, pageRequest);

        if (ObjectUtils.isEmpty(requestList)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequestDto.pageSize(), (int) totalCount));
        }

        List<ReceivedSubstituteRequestResponseDto> requests = requestList
            .stream()
            .map(ReceivedSubstituteRequestResponseDto::of)
            .toList();

        ReceivedSubstituteRequestListResponse last = requestList.getLast();

        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
            pageRequest.pageSize(),
            (int) totalCount
        );

        return CursorPaginatedApiResponse.of(pageResponseDto, requests);
    }
}
