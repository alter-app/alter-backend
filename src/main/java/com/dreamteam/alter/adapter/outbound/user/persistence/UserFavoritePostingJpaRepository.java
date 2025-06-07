package com.dreamteam.alter.adapter.outbound.user.persistence;

import com.dreamteam.alter.domain.user.entity.UserFavoritePosting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFavoritePostingJpaRepository extends JpaRepository<UserFavoritePosting, Long> {
}
