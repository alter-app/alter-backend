package com.dreamteam.alter.adapter.outbound.user.persistence;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserCertificateRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserCertificateRepositoryImpl implements UserCertificateRepository {

	private final UserCertificateJpaRepository userCertificateJpaRepository;

	@Override
	public void deleteAll(User user) {
		userCertificateJpaRepository.deleteAllByUser(user);
	}
}
