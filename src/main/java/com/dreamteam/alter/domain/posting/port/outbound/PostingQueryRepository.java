package com.dreamteam.alter.domain.posting.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingMapListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingMapMarkerFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.ManagerPostingListFilterDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.*;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface PostingQueryRepository {
    long getCountOfPostings(PostingListFilterDto filter);
    List<PostingListResponse> getPostingsWithCursor(CursorPageRequest<CursorDto> request, PostingListFilterDto filter, User user);
    PostingDetailResponse getPostingDetail(Long postingId, User user);
    Optional<Posting> findById(Long postingId);
    
    long getCountOfPostingMapList(PostingMapListFilterDto filter);
    List<PostingListResponse> getPostingMapListWithCursor(CursorPageRequest<CursorDto> request, PostingMapListFilterDto filter, User user);
    
    List<PostingListForMapMarkerResponse> getPostingListForMapMarker(PostingMapMarkerFilterDto filter);

    List<PostingListResponse> getWorkspacePostingList(Long workspaceId, User user);
    
    long getManagerPostingCount(ManagerUser managerUser, ManagerPostingListFilterDto filter);
    List<ManagerPostingListResponse> getManagerPostingsWithCursor(CursorPageRequest<CursorDto> request, ManagerUser managerUser, ManagerPostingListFilterDto filter);
    Optional<ManagerPostingDetailResponse> getManagerPostingDetail(Long postingId, ManagerUser managerUser);
    Optional<Posting> findByManagerAndId(Long postingId, ManagerUser managerUser);
    
    PostingFilterOptionsResponse getPostingFilterOptions();
}
