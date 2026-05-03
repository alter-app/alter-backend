package com.dreamteam.alter.application.user.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.DeleteUserProfileImageUseCase;

import lombok.RequiredArgsConstructor;

@Service("deleteUserProfileImage")
@RequiredArgsConstructor
@Transactional
public class DeleteUserProfileImage implements DeleteUserProfileImageUseCase {

	private final FileQueryRepository fileQueryRepository;

	@Override
	public void execute(User user) {
		File file = fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.USER_PROFILE,
				user.getId().toString())
			.orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

		file.markDeleted();
	}
}
