package com.dreamteam.alter.adapter.outbound.user.persistence;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.user.entity.UserSocial;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserSocialRepositoryImpl implements UserSocialRepository {

    private final UserSocialJpaRepository userSocialJpaRepository;

    @Override
    public void delete(UserSocial userSocial) {
        userSocialJpaRepository.delete(userSocial);
    }
}
