package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationRequestListResponseDto;
import com.dreamteam.alter.adapter.outbound.reputation.persistence.readonly.ReputationRequestListResponse;
import com.dreamteam.alter.common.util.CursorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

public abstract class AbstractGetReputationRequestList<A, F, C> {

    protected final ObjectMapper objectMapper;

    protected AbstractGetReputationRequestList(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public final CursorPaginatedApiResponse<ReputationRequestListResponseDto> execute(
        A actor,
        F filter,
        CursorPageRequestDto pageRequest
    ) {
        C context = prepare(actor, filter);

        validate(actor, filter, context);

        if (!canProceed(actor, filter, context)) {
            return empty(pageRequest.pageSize(), 0);
        }

        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(pageRequest.cursor())) {
            cursorDto = CursorUtil.decodeCursor(pageRequest.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> cursorPageRequest = CursorPageRequest.of(cursorDto, pageRequest.pageSize());

        long count = count(actor, filter, context);
        if (count == 0) {
            return empty(pageRequest.pageSize(), count);
        }

        List<ReputationRequestListResponse> rows = fetch(cursorPageRequest, actor, filter, context);
        if (ObjectUtils.isEmpty(rows)) {
            return empty(pageRequest.pageSize(), count);
        }

        ReputationRequestListResponse last = rows.getLast();
        CursorPageResponseDto pageResponse = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
            cursorPageRequest.pageSize(),
            (int) count
        );

        return CursorPaginatedApiResponse.of(
            pageResponse,
            rows.stream().map(ReputationRequestListResponseDto::of).toList()
        );
    }

    protected CursorPaginatedApiResponse<ReputationRequestListResponseDto> empty(int pageSize, long count) {
        return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageSize, (int) count));
    }

    protected abstract C prepare(A actor, F filter);

    protected abstract void validate(A actor, F filter, C context);

    protected boolean canProceed(A actor, F filter, C context) { return true; }

    protected abstract long count(A actor, F filter, C context);

    protected abstract List<ReputationRequestListResponse> fetch(
        CursorPageRequest<CursorDto> pageRequest,
        A actor,
        F filter,
        C context
    );

}
