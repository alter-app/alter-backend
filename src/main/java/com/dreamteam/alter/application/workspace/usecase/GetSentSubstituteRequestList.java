package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.SubstituteRequestResponseDto;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.GetSentSubstituteRequestListUseCase;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.SentSubstituteRequestListResponse;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getSentSubstituteRequestList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetSentSubstituteRequestList implements GetSentSubstituteRequestListUseCase {

    private final SubstituteRequestQueryRepository substituteRequestQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<SubstituteRequestResponseDto> execute(
        AppActor actor,
        SubstituteRequestStatus status,
        CursorPageRequestDto pageRequestDto
    ) {
        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(pageRequestDto.cursor())) {
            cursorDto = CursorUtil.decodeCursor(pageRequestDto.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> pageRequest = CursorPageRequest.of(cursorDto, pageRequestDto.pageSize());

        long totalCount = substituteRequestQueryRepository.getSentRequestCount(actor.getUser(), status);
        if (totalCount == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequestDto.pageSize(), (int) totalCount));
        }

        List<SentSubstituteRequestListResponse> requestList = substituteRequestQueryRepository
            .getSentRequestListWithCursor(actor.getUser(), status, pageRequest);

        if (ObjectUtils.isEmpty(requestList)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequestDto.pageSize(), (int) totalCount));
        }

        List<SubstituteRequestResponseDto> requests = requestList
            .stream()
            .map(SubstituteRequestResponseDto::of)
            .toList();

        SentSubstituteRequestListResponse last = requestList.getLast();

        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
            pageRequest.pageSize(),
            (int) totalCount
        );

        return CursorPaginatedApiResponse.of(pageResponseDto, requests);
    }
}
