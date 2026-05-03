package com.dreamteam.alter.application.user.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.CreateUserProfileImageUseCase;

import lombok.RequiredArgsConstructor;

@Service( "createUserProfileImage")
@RequiredArgsConstructor
@Transactional
public class CreateUserProfileImage implements CreateUserProfileImageUseCase {

	private final FileQueryRepository fileQueryRepository;
	private final AttachFilesUseCase attachFiles;

	@Override
	public void execute(User user, String fileId) {
		String targetId = user.getId().toString();
		fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.USER_PROFILE, targetId)
			.ifPresent(f -> {
				throw new CustomException(ErrorCode.FILE_ALREADY_ATTACHED);
			});

		attachFiles.execute(
			List.of(fileId),
			FileTargetType.USER_PROFILE,
			targetId,
			user.getId()
		);
	}
}
