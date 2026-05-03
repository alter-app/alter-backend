package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.domain.user.entity.User;

public interface UpdateUserProfileImageUseCase {
	void execute(User user, String fileId);
}
