package com.dreamteam.alter.adapter.outbound.user.persistence;

import com.dreamteam.alter.domain.user.entity.UserFavoritePosting;
import com.dreamteam.alter.domain.user.port.outbound.UserFavoritePostingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Transactional
public class UserFavoritePostingRepositoryImpl implements UserFavoritePostingRepository {

    private final UserFavoritePostingJpaRepository userFavoritePostingJpaRepository;

    @Override
    public void save(UserFavoritePosting favoritePosting) {
        userFavoritePostingJpaRepository.save(favoritePosting);
    }

    @Override
    public void delete(UserFavoritePosting favoritePosting) {
        userFavoritePostingJpaRepository.delete(favoritePosting);
    }

}
