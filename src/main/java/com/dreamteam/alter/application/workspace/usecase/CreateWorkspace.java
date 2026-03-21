package com.dreamteam.alter.application.workspace.usecase;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestDto;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserRepository;
import com.dreamteam.alter.domain.user.type.ManagerUserStatus;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;

import lombok.RequiredArgsConstructor;

@Service("createWorkspace")
@RequiredArgsConstructor
@Transactional
public class CreateWorkspace implements CreateWorkspaceUseCase {

	private final ManagerUserRepository managerUserRepository;
	private final WorkspaceRepository workspaceRepository;
	private final AttachFilesUseCase attachFiles;

	@Override
	public void execute(AppActor actor, CreateWorkspaceRequestDto request) {
		ManagerUser managerUser = managerUserRepository.save(
			ManagerUser.create(actor.getUser(), ManagerUserStatus.PENDING)
		);

		Workspace workspace = Workspace.create(
			managerUser,
			request.getBrn(),
			request.getBizName(),
			request.getType(),
			request.getContact(),
			null,
			WorkspaceStatus.PENDING,
			request.getAddress(),
			request.getProvince(),
			request.getDistrict(),
			request.getTown(),
			request.getLatitude(),
			request.getLongitude()
		);

		String savedWorkspaceId = workspaceRepository.save(workspace).toString();
		Long userId = actor.getUserId();

		Map<String, FileTargetType> fileMap = new HashMap<>();
		fileMap.put(request.getWorkspaceCertFileId(), FileTargetType.WORKSPACE_CERTIFICATE);
		fileMap.put(request.getWorkspaceOwnIdentityFileId(), FileTargetType.WORKSPACE_OWN_IDENTITY);
		if (request.getWorkspaceWarrantFileId() != null) {
			fileMap.put(request.getWorkspaceWarrantFileId(), FileTargetType.WORKSPACE_WARRANT);
		}
		attachFiles.executeMap(fileMap, savedWorkspaceId, userId);
	}
}
