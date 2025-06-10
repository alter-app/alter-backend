package com.dreamteam.alter.domain.user.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.UserFavoritePostingListResponse;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.entity.UserFavoritePosting;

import java.util.List;
import java.util.Optional;

public interface UserFavoritePostingQueryRepository {
    long getCountByUser(User user);
    List<UserFavoritePostingListResponse> findUserFavoritePostingListWithCursor(User user, CursorPageRequest<CursorDto> request);
    Optional<UserFavoritePosting> findByPostingIdAndUser(Long favoritePostingId, User user);
}
