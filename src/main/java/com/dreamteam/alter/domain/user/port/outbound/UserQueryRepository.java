package com.dreamteam.alter.domain.user.port.outbound;

import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.UserSelfInfoResponse;
import com.dreamteam.alter.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserQueryRepository {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
    Optional<User> findByContact(String contact);
    Optional<User> findByEmailAndContact(String email, String contact);
    Optional<UserSelfInfoResponse> getUserSelfInfoSummary(Long id);
    List<User> findAllById(List<Long> ids);
}
