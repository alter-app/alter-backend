package com.dreamteam.alter.domain.posting.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingDetailResponse;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingListResponse;
import com.dreamteam.alter.domain.posting.entity.Posting;

import java.util.List;
import java.util.Optional;

public interface PostingQueryRepository {
    long getCountOfPostings();
    List<PostingListResponse> getPostingsWithCursor(CursorPageRequest<CursorDto> request);
    PostingDetailResponse getPostingDetail(Long postingId);
    Optional<Posting> findById(Long postingId);
}
