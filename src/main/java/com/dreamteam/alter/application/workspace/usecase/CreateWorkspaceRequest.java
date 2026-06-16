package com.dreamteam.alter.application.workspace.usecase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.common.dto.WorkspaceImageRequestDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestDto;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequestImage;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestImageRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestRepository;

import lombok.RequiredArgsConstructor;

@Service("createWorkspaceRequest")
@RequiredArgsConstructor
@Transactional
public class CreateWorkspaceRequest implements CreateWorkspaceRequestUseCase {

	private final WorkspaceRequestRepository workspaceRequestRepository;
	private final WorkspaceRequestImageRepository workspaceRequestImageRepository;
	private final AttachFilesUseCase attachFiles;

	@Override
	public void execute(User user, CreateWorkspaceRequestDto request) {
		WorkspaceRequest workspaceRequest = WorkspaceRequest.create(
			user,
			request.getBrn(),
			request.getBizName(),
			request.getType(),
			request.getContact(),
			request.getAddress(),
			request.getProvince(),
			request.getDistrict(),
			request.getTown(),
			request.getLatitude(),
			request.getLongitude()
		);

		Long savedWorkspaceRequestId = workspaceRequestRepository.save(workspaceRequest);

		Map<String, FileTargetType> fileMap = new HashMap<>();
		fileMap.put(request.getWorkspaceCertFileId(), FileTargetType.WORKSPACE_CERTIFICATE);
		fileMap.put(request.getWorkspaceOwnIdentityFileId(), FileTargetType.WORKSPACE_OWN_IDENTITY);
		if (request.getWorkspaceWarrantFileId() != null) {
			fileMap.put(request.getWorkspaceWarrantFileId(), FileTargetType.WORKSPACE_WARRANT);
		}
		attachFiles.executeMap(fileMap, savedWorkspaceRequestId.toString(), user.getId());

		List<String> representativeImageFileIds =
			WorkspaceImageRequestDto.toOrderedFileIds(request.getRepresentativeImages());
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
