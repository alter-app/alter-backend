package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.*;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserFavoritePostingListResponseDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.UserFavoritePostingListResponse;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.GetUserFavoritePostingListUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserFavoritePostingQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("getUserFavoritePostingList")
@RequiredArgsConstructor
@Transactional
public class GetUserFavoritePostingList implements GetUserFavoritePostingListUseCase {

    private final UserFavoritePostingQueryRepository userFavoritePostingQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<UserFavoritePostingListResponseDto> execute(
        AppActor actor,
        CursorPageRequestDto request
    ) {
        User user = actor.getUser();

        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(request.cursor())) {
            cursorDto = CursorUtil.decodeCursor(request.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> pageRequest = CursorPageRequest.of(cursorDto, request.pageSize());

        long count = userFavoritePostingQueryRepository.getCountByUser(user);
        if (count == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(request.pageSize(), (int) count));
        }

        List<UserFavoritePostingListResponse> favoritePostings = userFavoritePostingQueryRepository.findUserFavoritePostingListWithCursor(user, pageRequest);
        if (ObjectUtils.isEmpty(favoritePostings)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(request.pageSize(), (int) count));
        }

        UserFavoritePostingListResponse last = favoritePostings.getLast();
        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
            pageRequest.pageSize(),
            (int) count
        );

        return CursorPaginatedApiResponse.of(
            pageResponseDto,
            favoritePostings.stream()
                .map(com.dreamteam.alter.adapter.inbound.general.user.dto.UserFavoritePostingListResponseDto::from)
                .toList()
        );
    }

}
