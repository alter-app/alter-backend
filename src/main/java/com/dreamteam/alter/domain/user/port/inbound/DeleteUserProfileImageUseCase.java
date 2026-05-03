package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.domain.user.entity.User;

public interface DeleteUserProfileImageUseCase {
	void execute(User user);
}
