package com.dreamteam.alter.application.user.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.UpdateUserProfileImageUseCase;

import lombok.RequiredArgsConstructor;

@Service("updateUserProfileImage")
@RequiredArgsConstructor
@Transactional
public class UpdateUserProfileImage implements UpdateUserProfileImageUseCase {

	private final FileQueryRepository fileQueryRepository;
	private final AttachFilesUseCase attachFiles;

	@Override
	public void execute(User user, String fileId) {
		String targetId = user.getId().toString();
		fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.USER_PROFILE, targetId)
			.ifPresent(File::markDeleted);

		attachFiles.execute(
			List.of(fileId),
			FileTargetType.USER_PROFILE,
			targetId,
			user.getId()
		);
	}
}
