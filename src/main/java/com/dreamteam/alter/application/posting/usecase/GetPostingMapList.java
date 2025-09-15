package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.*;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingMapListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingMapListResponseDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingListResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.posting.port.inbound.GetPostingMapListUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getPostingMapList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPostingMapList implements GetPostingMapListUseCase {

    private final PostingQueryRepository postingQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<PostingMapListResponseDto> execute(
        CursorPageRequestDto request,
        PostingMapListFilterDto filter,
        AppActor actor
    ) {
        if (ObjectUtils.isEmpty(filter)) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "좌표 값은 필수입니다.");
        }

        if (ObjectUtils.isEmpty(filter.getCoordinate1()) || ObjectUtils.isEmpty(filter.getCoordinate2())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "좌표 값은 좌상단, 우하단 모두 포함해야합니다.");
        }

        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(request.cursor())) {
            cursorDto = CursorUtil.decodeCursor(request.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> pageRequest = CursorPageRequest.of(cursorDto, request.pageSize());

        long count = postingQueryRepository.getCountOfPostingMapList(filter);
        if (count == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(request.pageSize(), (int) count));
        }

        // 좌표 범위 내의 공고 목록을 커서 페이징으로 조회
        List<PostingListResponse> postings = postingQueryRepository.getPostingMapListWithCursor(pageRequest, filter, actor.getUser());
        if (ObjectUtils.isEmpty(postings)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(request.pageSize(), (int) count));
        }

        PostingListResponse last = postings.getLast();
        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
            pageRequest.pageSize(),
            (int) count
        );

        return CursorPaginatedApiResponse.of(
            pageResponseDto,
            postings.stream()
                .map(PostingMapListResponseDto::from)
                .toList()
        );
    }
}
