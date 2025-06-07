package com.dreamteam.alter.domain.user.port.outbound;

import com.dreamteam.alter.domain.user.entity.UserFavoritePosting;

public interface UserFavoritePostingRepository {
    void save(UserFavoritePosting favoritePosting);
    void delete(UserFavoritePosting favoritePosting);
}
