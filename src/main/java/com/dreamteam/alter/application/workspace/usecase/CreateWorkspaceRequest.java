package com.dreamteam.alter.application.workspace.usecase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.StringUtils;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.util.PhoneNumberUtil;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.command.CreateWorkspaceRequestCommand;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequestImage;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessTypeRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestImageRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestRepository;

import lombok.RequiredArgsConstructor;

@Service("createWorkspaceRequest")
@RequiredArgsConstructor
@Transactional
public class CreateWorkspaceRequest implements CreateWorkspaceRequestUseCase {

	private static final int MAX_IMAGE_COUNT = 5;

	private final WorkspaceRequestRepository workspaceRequestRepository;
	private final WorkspaceRequestImageRepository workspaceRequestImageRepository;
	private final BusinessTypeRepository businessTypeRepository;
	private final AttachFilesUseCase attachFiles;

	@Override
	public void execute(CreateWorkspaceRequestCommand request) {
		if (StringUtils.equals(request.workspaceCertFileId(), request.workspaceOwnIdentityFileId())
			|| (request.workspaceWarrantFileId() != null
				&& (StringUtils.equals(request.workspaceWarrantFileId(), request.workspaceCertFileId())
					|| StringUtils.equals(request.workspaceWarrantFileId(), request.workspaceOwnIdentityFileId())))) {
			throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "증빙 서류는 역할별로 서로 다른 파일을 첨부해야 합니다.");
		}
		User user = request.user();
		BusinessType businessType = businessTypeRepository.findById(request.businessTypeId())
			.orElseThrow(() -> new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "존재하지 않는 업종입니다."));

		WorkspaceRequest workspaceRequest = WorkspaceRequest.create(
			user,
			request.brn(),
			request.bizName(),
			businessType,
			request.businessTypeDetail(),
			PhoneNumberUtil.normalizeLocalNumber(request.contact()),
			request.address(),
			request.province(),
			request.district(),
			request.town(),
			request.latitude(),
			request.longitude()
		);

		Long savedWorkspaceRequestId = workspaceRequestRepository.save(workspaceRequest);

		Map<String, FileTargetType> fileMap = new HashMap<>();
		fileMap.put(request.workspaceCertFileId(), FileTargetType.WORKSPACE_CERTIFICATE);
		fileMap.put(request.workspaceOwnIdentityFileId(), FileTargetType.WORKSPACE_OWN_IDENTITY);
		if (request.workspaceWarrantFileId() != null) {
			fileMap.put(request.workspaceWarrantFileId(), FileTargetType.WORKSPACE_WARRANT);
		}
		attachFiles.executeMap(fileMap, savedWorkspaceRequestId.toString(), user.getId());

		List<String> representativeImageFileIds = request.representativeImageFileIds();
		if (representativeImageFileIds.size() > MAX_IMAGE_COUNT) {
			throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "대표이미지는 최대 " + MAX_IMAGE_COUNT + "개까지 등록할 수 있습니다.");
		}
		if (!representativeImageFileIds.isEmpty()) {
			attachFiles.execute(
				representativeImageFileIds,
				FileTargetType.WORKSPACE_REPRESENTATIVE_IMAGE,
				savedWorkspaceRequestId.toString(),
				user.getId()
			);

			List<WorkspaceRequestImage> images = new ArrayList<>();
			for (int sortOrder = 0; sortOrder < representativeImageFileIds.size(); sortOrder++) {
				images.add(WorkspaceRequestImage.create(workspaceRequest, representativeImageFileIds.get(sortOrder), sortOrder));
			}
			workspaceRequestImageRepository.saveAll(images);
		}
	}
}
