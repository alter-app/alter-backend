package com.dreamteam.alter.application.workspace.usecase;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestDto;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestRepository;

import lombok.RequiredArgsConstructor;

@Service("createWorkspace")
@RequiredArgsConstructor
@Transactional
public class CreateWorkspaceRequest implements CreateWorkspaceRequestUseCase {

	private final WorkspaceRequestRepository workspaceRequestRepository;
	private final AttachFilesUseCase attachFiles;

	@Override
	public void execute(AppActor actor, CreateWorkspaceRequestDto request) {
		WorkspaceRequest workspaceRequest = WorkspaceRequest.create(
			actor.getUser(),
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
		attachFiles.executeMap(fileMap, savedWorkspaceRequestId.toString(), actor.getUserId());
	}
}
