package com.dreamteam.alter.domain.user.port.outbound;

import com.dreamteam.alter.domain.user.entity.User;

public interface UserCertificateRepository {
	void deleteAll(User user);
}
