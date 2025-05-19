package com.dreamteam.alter.domain.posting.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingListResponse;

import java.util.List;

public interface PostingQueryRepository {
    long getCountOfPostings();
    List<PostingListResponse> getPostingsWithCursor(CursorPageRequest<CursorDto> request);
}
