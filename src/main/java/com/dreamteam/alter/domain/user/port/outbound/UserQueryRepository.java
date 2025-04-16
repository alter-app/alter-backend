package com.dreamteam.alter.domain.user.port.outbound;

import com.dreamteam.alter.domain.user.entity.User;

public interface UserQueryRepository {
    User findBySocialId(String socialId);
    User findByEmail(String email);
    User findByNickname(String nickname);
}
