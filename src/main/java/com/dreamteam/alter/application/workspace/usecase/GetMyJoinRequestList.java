package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.*;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyJoinRequestListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyJoinRequestResponseDto;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.port.inbound.GetMyJoinRequestListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessJoinRequestQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getMyJoinRequestList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyJoinRequestList implements GetMyJoinRequestListUseCase {

    private final BusinessJoinRequestQueryRepository businessJoinRequestQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<MyJoinRequestResponseDto> execute(AppActor actor, MyJoinRequestListFilterDto filter, CursorPageRequestDto cursorPageRequest) {
        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(cursorPageRequest.cursor())) {
            cursorDto = CursorUtil.decodeCursor(cursorPageRequest.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> pageRequest = CursorPageRequest.of(cursorDto, cursorPageRequest.pageSize());

        long count = businessJoinRequestQueryRepository.countByUser(actor.getUser(), filter);
        if (count == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(cursorPageRequest.pageSize(), (int) count));
        }

        List<BusinessJoinRequest> requests = businessJoinRequestQueryRepository.findByUserWithCursor(pageRequest, actor.getUser(), filter);
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
                .map(MyJoinRequestResponseDto::from)
                .toList()
        );
    }
}
