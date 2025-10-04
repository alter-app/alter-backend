package com.dreamteam.alter.domain.posting.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserPostingApplicationListFilterDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.ManagerPostingApplicationDetailResponse;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.UserPostingApplicationListResponse;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.ManagerPostingApplicationListResponse;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface PostingApplicationQueryRepository {
    long getCountByUser(User user, UserPostingApplicationListFilterDto filter);
    List<UserPostingApplicationListResponse> getUserPostingApplicationListWithCursor(User user, CursorPageRequest<CursorDto> pageRequest, UserPostingApplicationListFilterDto filter);
    Optional<PostingApplication> getUserPostingApplication(User user, Long applicationId);

    long getManagerPostingApplicationCount(
        ManagerUser managerUser,
        PostingApplicationListFilterDto filter
    );
    List<ManagerPostingApplicationListResponse> getManagerPostingApplicationListWithCursor(
        ManagerUser managerUser,
        CursorPageRequest<CursorDto> request,
        PostingApplicationListFilterDto filter
    );
    Optional<ManagerPostingApplicationDetailResponse> getManagerPostingApplicationDetail(
        ManagerUser managerUser,
        Long postingApplicationId
    );

    Optional<PostingApplication> getByManagerAndId(ManagerUser managerUser, Long postingApplicationId);

}
