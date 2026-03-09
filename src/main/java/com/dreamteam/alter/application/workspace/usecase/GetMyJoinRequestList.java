package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyJoinRequestListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyJoinRequestResponseDto;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.port.inbound.GetMyJoinRequestListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessJoinRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.type.BusinessJoinRequestStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service("getMyJoinRequestList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyJoinRequestList implements GetMyJoinRequestListUseCase {

    private final BusinessJoinRequestQueryRepository businessJoinRequestQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<MyJoinRequestResponseDto> execute(AppActor actor, MyJoinRequestListFilterDto filter, CursorPageRequestDto cursorPageRequest) {
        BusinessJoinRequestStatus status = ObjectUtils.isNotEmpty(filter) ? filter.getStatus() : null;
        LocalDateTime from = ObjectUtils.isNotEmpty(filter) && filter.getFrom() != null ? filter.getFrom().atStartOfDay() : null;
        LocalDateTime to = ObjectUtils.isNotEmpty(filter) && filter.getTo() != null ? filter.getTo().plusDays(1).atStartOfDay() : null;

        long totalCount = businessJoinRequestQueryRepository.countByUser(actor.getUser(), status, from, to);
        if (totalCount == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(cursorPageRequest.pageSize(), (int) totalCount));
        }

        CursorDto cursor = StringUtils.hasText(cursorPageRequest.cursor())
            ? CursorUtil.decodeCursor(cursorPageRequest.cursor(), CursorDto.class, objectMapper)
            : null;

        List<BusinessJoinRequest> requests = businessJoinRequestQueryRepository.findByUserWithCursor(
            actor.getUser(), status, from, to, cursor, cursorPageRequest.pageSize() + 1
        );

        if (ObjectUtils.isEmpty(requests)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(cursorPageRequest.pageSize(), (int) totalCount));
        }

        boolean hasNext = requests.size() > cursorPageRequest.pageSize();
        List<BusinessJoinRequest> pageItems = hasNext
            ? requests.subList(0, cursorPageRequest.pageSize())
            : requests;

        String nextCursor = hasNext
            ? CursorUtil.encodeCursor(
                new CursorDto(pageItems.getLast().getId(), pageItems.getLast().getCreatedAt()),
                objectMapper)
            : null;

        List<MyJoinRequestResponseDto> data = pageItems.stream()
            .map(MyJoinRequestResponseDto::from)
            .toList();

        return CursorPaginatedApiResponse.of(
            CursorPageResponseDto.of(nextCursor, cursorPageRequest.pageSize(), (int) totalCount),
            data
        );
    }
}
