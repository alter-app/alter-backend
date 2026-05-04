package com.dreamteam.alter.adapter.outbound.user.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.entity.UserCertificate;

public interface UserCertificateJpaRepository extends JpaRepository<UserCertificate, Long> {
	void deleteAllByUser(User user);
}
