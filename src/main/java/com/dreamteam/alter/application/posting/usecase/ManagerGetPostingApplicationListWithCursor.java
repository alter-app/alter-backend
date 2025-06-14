package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.*;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationListResponseDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.ManagerPostingApplicationListResponse;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.posting.port.inbound.ManagerGetPostingApplicationListWithCursorUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("managerGetPostingApplicationListWithCursor")
@RequiredArgsConstructor
@Transactional
public class ManagerGetPostingApplicationListWithCursor implements ManagerGetPostingApplicationListWithCursorUseCase {

    private final PostingApplicationQueryRepository postingApplicationQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<PostingApplicationListResponseDto> execute(
        CursorPageRequestDto request,
        PostingApplicationListFilterDto filter,
        ManagerActor actor
    ) {
        ManagerUser managerUser = actor.getManagerUser();

        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(request.cursor())) {
            cursorDto = CursorUtil.decodeCursor(request.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> pageRequest = CursorPageRequest.of(cursorDto, request.pageSize());

        long count = postingApplicationQueryRepository.getManagerPostingApplicationCount(managerUser, filter);
        if (count == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(request.pageSize(), (int) count));
        }

        List<ManagerPostingApplicationListResponse> postingApplications =
            postingApplicationQueryRepository.getManagerPostingApplicationListWithCursor(
                managerUser,
                pageRequest,
                filter
            );
        if (ObjectUtils.isEmpty(postingApplications)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(request.pageSize(), (int) count));
        }

        ManagerPostingApplicationListResponse last = postingApplications.getLast();
        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
            pageRequest.pageSize(),
            (int) count
        );

        return CursorPaginatedApiResponse.of(
            pageResponseDto,
            postingApplications.stream()
                .map(PostingApplicationListResponseDto::from)
                .toList()
        );
    }

}
