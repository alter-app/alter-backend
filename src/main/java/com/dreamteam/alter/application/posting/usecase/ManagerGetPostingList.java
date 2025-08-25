package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.*;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.ManagerPostingListResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.ManagerPostingListFilterDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.ManagerPostingListResponse;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.posting.port.inbound.ManagerGetPostingListUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("managerGetPostingList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerGetPostingList implements ManagerGetPostingListUseCase {

    private final PostingQueryRepository postingQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<ManagerPostingListResponseDto> execute(CursorPageRequestDto request, ManagerPostingListFilterDto filter, ManagerActor actor) {
        ManagerUser managerUser = actor.getManagerUser();

        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(request.cursor())) {
            cursorDto = CursorUtil.decodeCursor(request.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> pageRequest = CursorPageRequest.of(cursorDto, request.pageSize());

        long count = postingQueryRepository.getManagerPostingCount(managerUser, filter);
        if (count == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(request.pageSize(), (int) count));
        }

        List<ManagerPostingListResponse> postings = postingQueryRepository.getManagerPostingsWithCursor(pageRequest, managerUser, filter);
        if (ObjectUtils.isEmpty(postings)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(request.pageSize(), (int) count));
        }

        ManagerPostingListResponse last = postings.getLast();
        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
            pageRequest.pageSize(),
            (int) count
        );

        return CursorPaginatedApiResponse.of(
            pageResponseDto,
            postings.stream()
                .map(ManagerPostingListResponseDto::from)
                .toList()
        );
    }
}
