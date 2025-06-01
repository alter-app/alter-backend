package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.*;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingListResponseDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingListResponse;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.posting.port.inbound.GetPostingsWithCursorUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getPostingsWithCursor")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPostingsWithCursor implements GetPostingsWithCursorUseCase {

    private final PostingQueryRepository postingQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<PostingListResponseDto> execute(CursorPageRequestDto request) {
        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(request.cursor())) {
            cursorDto = CursorUtil.decodeCursor(request.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> pageRequest = CursorPageRequest.of(cursorDto, request.pageSize());

        long count = postingQueryRepository.getCountOfPostings();
        if (count == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(request.pageSize(), (int) count));
        }

        List<PostingListResponse> postings = postingQueryRepository.getPostingsWithCursor(pageRequest);
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
                .map(PostingListResponseDto::from)
                .toList()
        );
    }

}
