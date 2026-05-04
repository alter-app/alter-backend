package com.dreamteam.alter.adapter.outbound.user.persistence;

import java.util.List;

import com.dreamteam.alter.domain.user.entity.UserSocial;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserSocialRepositoryImpl implements UserSocialRepository {

    private final UserSocialJpaRepository userSocialJpaRepository;

    @Override
    public void delete(UserSocial userSocial) {
        userSocialJpaRepository.delete(userSocial);
    }

    @Override
    public void deleteAll(List<UserSocial> userSocials) {
        userSocialJpaRepository.deleteAll(userSocials);
    }
}
