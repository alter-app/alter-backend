package com.dreamteam.alter.adapter.outbound.user.persistence;

import com.dreamteam.alter.domain.user.entity.UserSocial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSocialJpaRepository extends JpaRepository<UserSocial, Long> {
}
