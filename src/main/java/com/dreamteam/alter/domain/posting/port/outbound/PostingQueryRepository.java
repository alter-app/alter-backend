package com.dreamteam.alter.domain.posting.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.ManagerPostingListFilterDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.ManagerPostingDetailResponse;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.ManagerPostingListResponse;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingDetailResponse;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingListResponse;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface PostingQueryRepository {
    long getCountOfPostings();
    List<PostingListResponse> getPostingsWithCursor(CursorPageRequest<CursorDto> request, User user);
    PostingDetailResponse getPostingDetail(Long postingId, User user);
    Optional<Posting> findById(Long postingId);
    
    long getManagerPostingCount(ManagerUser managerUser, ManagerPostingListFilterDto filter);
    List<ManagerPostingListResponse> getManagerPostingsWithCursor(CursorPageRequest<CursorDto> request, ManagerUser managerUser, ManagerPostingListFilterDto filter);
    Optional<ManagerPostingDetailResponse> getManagerPostingDetail(Long postingId, ManagerUser managerUser);
}
