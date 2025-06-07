package com.dreamteam.alter.domain.user.port.outbound;

import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.UserSelfInfoResponse;
import com.dreamteam.alter.domain.user.entity.User;

import java.util.Optional;

public interface UserQueryRepository {
    Optional<User> findById(Long id);
    User findBySocialId(String socialId);
    User findByEmail(String email);
    User findByNickname(String nickname);
    Optional<UserSelfInfoResponse> getUserSelfInfoSummary(Long id);
}
