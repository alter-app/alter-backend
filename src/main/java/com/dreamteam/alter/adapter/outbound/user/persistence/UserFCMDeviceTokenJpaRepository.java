package com.dreamteam.alter.adapter.outbound.user.persistence;

import com.dreamteam.alter.domain.user.entity.FcmDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFCMDeviceTokenJpaRepository extends JpaRepository<FcmDeviceToken, Long> {
}
