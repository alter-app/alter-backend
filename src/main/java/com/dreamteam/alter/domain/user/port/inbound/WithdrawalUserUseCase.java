package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.domain.user.entity.User;

public interface WithdrawalUserUseCase {
	void execute(User user);
}
